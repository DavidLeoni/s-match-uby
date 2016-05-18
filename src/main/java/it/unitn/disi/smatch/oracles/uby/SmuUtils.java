package it.unitn.disi.smatch.oracles.uby;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import javax.annotation.Nullable;

import de.tudarmstadt.ukp.lmf.hibernate.HibernateConnect;
import de.tudarmstadt.ukp.lmf.model.core.LexicalResource;

import static de.tudarmstadt.ukp.lmf.model.enums.ERelNameSemantics.*;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;


/**
 * Utility class for S-match Uby
 * 
 * @since 0.1
 */
public final class SmuUtils {

	private static Map<String, String> inverseRelations = new HashMap();

	static {
		putInverseRelations(ANTONYM, ANTONYM);
		putInverseRelations(HYPERNYM, HYPONYM);
		putInverseRelations(MERONYMMEMBER, HOLONYMMEMBER);
		putInverseRelations(MERONYMSUBSTANCE, HOLONYMSUBSTANCE);
		putInverseRelations(MERONYMPART, HOLONYMPART);
		putInverseRelations(HYPERNYMINSTANCE, HYPONYMINSTANCE);
	}

	private static Map<DBConfig, Configuration> cachedHibernateConfigurations = new HashMap();

	private SmuUtils() {
	}

	/**
	 * Sets {@code a} as {@code b}'s symmetric type, and vice versa.
	 *
	 * @param a
	 *            pointer type
	 * @param b
	 *            pointer type
	 */
	private static void putInverseRelations(String a, String b) {
		checkNotEmpty(a, "Invalid first relation!");
		checkNotEmpty(b, "Invalid second relation!");

		inverseRelations.put(a, b);
		inverseRelations.put(b, a);
	}

	/**
	 * @throws SmuNotFoundException
	 *             if {code relation} does not have an inverse
	 */
	public static String getInverse(String relation) {
		checkNotEmpty(relation, "Invalid relation!");

		String ret = inverseRelations.get(relation);
		if (ret == null) {
			throw new SmuNotFoundException("Couldn't find the relation " + relation);
		}
		return ret;
	}

	/**
	 * Returns true if provided relation has inverse.
	 */
	public static boolean hasInverse(String relation) {
		checkNotEmpty(relation, "Invalid relation!");

		String ret = inverseRelations.get(relation);
		if (ret == null) {
			return false;
		}
		return true;
	}

	/**
	 * Note: if false is returned it means we <i> don't know </i> the relations
	 * are actually inverses.
	 */
	public static boolean isInverse(String a, String b) {
		checkNotEmpty(a, "Invalid first relation!");
		checkNotEmpty(b, "Invalid second relation!");

		String inverse = inverseRelations.get(a);
		if (inverse == null) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * Mappings from Uby classes to out own custom ones.
	 */
	private static LinkedHashMap<String, String> customClassMappings;

	static {
		customClassMappings = new LinkedHashMap();
		customClassMappings.put(de.tudarmstadt.ukp.lmf.model.semantics.SynsetRelation.class.getCanonicalName(),
				SmuSynsetRelation.class.getCanonicalName());
	}

	private static final Logger log = LoggerFactory.getLogger(SmuUtils.class);

	/**
	 * Create all LMF Tables in the database based on the hibernate mapping
	 * 
	 * (adapted from LMFDBUtils.createTables(dbConfig) )
	 * 
	 * @param dbConfig
	 * @throws FileNotFoundException
	 * @since 0.1
	 */
	public static void createTables(DBConfig dbConfig) throws FileNotFoundException {

		log.info("CREATE S-MATCH UBY TABLES");

		Configuration hcfg = getHibernateConfig(dbConfig);

		hcfg.setProperty("hibernate.hbm2ddl.auto", "none");
		SchemaExport se = new SchemaExport(hcfg);
		se.create(true, true);
	}

	/**
	 * Loads a given xml hibernate configuration xml into {@code hcfg}
	 *
	 * @since 0.1
	 */
	public static void loadHibernateXml(Configuration hcfg, Resource xml) {

		log.info("Loading config " + xml.getDescription() + " ...");

		try {

			java.util.Scanner sc = new java.util.Scanner(xml.getInputStream()).useDelimiter("\\A");
			String s = sc.hasNext() ? sc.next() : "";
			sc.close();

			for (Map.Entry<String, String> e : customClassMappings.entrySet()) {
				s = s.replace(e.getKey(), e.getValue());
			}
			hcfg.addXML(s);

		} catch (Exception e) {
			throw new RuntimeException("Error while reading file at path: " + xml.getDescription(), e);
		}

	}

	/**
	 * 
	 * @param dbConfig
	 *            * @since 0.1
	 */
	public static Configuration getHibernateConfig(DBConfig dbConfig) {

		if (cachedHibernateConfigurations.get(dbConfig) != null) {
			log.debug("Returning cached configuration.");
			return cachedHibernateConfigurations.get(dbConfig);
		}

		log.info("Going to load configuration...");

		Configuration hcfg = new Configuration()
				.addProperties(HibernateConnect.getProperties(dbConfig.getJdbc_url(), dbConfig.getJdbc_driver_class(),
						dbConfig.getDb_vendor(), dbConfig.getUser(), dbConfig.getPassword(), dbConfig.isShowSQL()));

		// load hibernate mappings
		ClassLoader cl = HibernateConnect.class.getClassLoader();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
		Resource[] mappings = null;
		try {
			mappings = resolver.getResources("hibernatemap/access/**/*.hbm.xml");
			for (Resource mapping : mappings) {
				boolean isCustomized = false;
				for (String c : customClassMappings.keySet()) {
					String[] cs = c.split("\\.");
					String cn = cs[cs.length - 1];
					if (mapping.getFilename().replace(".hbm.xml", "").contains(cn)) {
						isCustomized = true;
					}
				}
				if (isCustomized) {
					log.info("Skipping class customized by Smatch Uby: " + mapping.getDescription());
				} else {
					loadHibernateXml(hcfg, mapping);
				}
			}

		} catch (IOException e) {
			throw new RuntimeException("Error while loading hibernate mappings!", e);
		}

		log.info("Loading custom S-Match Uby hibernate mappings... ");

		try {

			Resource[] resources = new PathMatchingResourcePatternResolver(
					SmuUtils.class.getClassLoader()).getResources("hybernatemap/access/**/*.hbm.xml");
			
			SmuUtils.checkArgument(resources.length == 1, 
					"Resource should be equals to 1, found instead " + resources.length);

			for (Resource r : resources) {
				loadHibernateXml(hcfg, r);
			}

		} catch (Exception e) {
			throw new RuntimeException("Hibernate mappings not found!", e);
		}

		log.info("Done loading custom mappings. ");

		cachedHibernateConfigurations.put(dbConfig, hcfg);
		return hcfg;
	}

	/**
	 *
	 * Checks if provided string is non null and non empty.
	 *
	 * @param prependedErrorMessage
	 *            the exception message to use if the check fails; will be
	 *            converted to a string using String.valueOf(Object) and
	 *            prepended to more specific error messages.
	 *
	 * @throws IllegalArgumentException
	 *             if provided string fails validation
	 *
	 * @return the non-empty string that was validated
	 * @since 0.1
	 */
	public static String checkNotEmpty(String string, @Nullable Object prependedErrorMessage) {
		checkArgument(string != null, "%s -- Reason: Found null string.", prependedErrorMessage);
		if (string.length() == 0) {
			throw new IllegalArgumentException(
					String.valueOf(prependedErrorMessage) + " -- Reason: Found empty string.");
		}
		return string;
	}

	/**
	 *
	 * Checks if provided string is non null and non empty.
	 *
	 * @param errorMessageTemplate
	 *            a template for the exception message should the check fail.
	 *            The message is formed by replacing each {@code %s} placeholder
	 *            in the template with an argument. These are matched by
	 *            position - the first {@code %s} gets {@code
	 *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to
	 *            the formatted message in square braces. Unmatched placeholders
	 *            will be left as-is.
	 * @param errorMessageArgs
	 *            the arguments to be substituted into the message template.
	 *            Arguments are converted to strings using
	 *            {@link String#valueOf(Object)}.
	 * @throws IllegalArgumentException
	 *             if {@code expression} is false
	 * @throws NullPointerException
	 *             if the check fails and either {@code errorMessageTemplate} or
	 *             {@code errorMessageArgs} is null (don't let this happen)
	 *
	 *
	 * @throws IllegalArgumentException
	 *             if provided string fails validation
	 *
	 * @return the non-empty string that was validated
	 * @since 0.1
	 */
	public static String checkNotEmpty(String string, @Nullable String errorMessageTemplate,
			@Nullable Object... errorMessageArgs) {
		String formattedMessage = SmuUtils.format(errorMessageTemplate, errorMessageArgs);
		checkArgument(string != null, "%s -- Reason: Found null string.", formattedMessage);
		if (string.length() == 0) {
			throw new IllegalArgumentException(formattedMessage + " -- Reason: Found empty string.");
		}
		return string;
	}

	/**
	 *
	 * Substitutes each {@code %s} in {@code template} with an argument. These
	 * are matched by position: the first {@code %s} gets {@code args[0]}, etc.
	 * If there are more arguments than placeholders, the unmatched arguments
	 * will be appended to the end of the formatted message in square braces.
	 * <br/>
	 * <br/>
	 * (Copied from Guava's
	 * {@link com.google.common.base.Preconditions#format(java.lang.String, java.lang.Object...) }
	 * )
	 *
	 * @param template
	 *            a non-null string containing 0 or more {@code %s}
	 *            placeholders.
	 * @param args
	 *            the arguments to be substituted into the message template.
	 *            Arguments are converted to strings using
	 *            {@link String#valueOf(Object)}. Arguments can be null.
	 *
	 * @since 0.1
	 */
	public static String format(String template, @Nullable Object... args) {
		if (template == null) {
			log.warn("Found null template while formatting, converting it to \"null\"");
		}
		template = String.valueOf(template); // null -> "null"

		// start substituting the arguments into the '%s' placeholders
		StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
		int templateStart = 0;
		int i = 0;
		while (i < args.length) {
			int placeholderStart = template.indexOf("%s", templateStart);
			if (placeholderStart == -1) {
				break;
			}
			builder.append(template.substring(templateStart, placeholderStart));
			builder.append(args[i++]);
			templateStart = placeholderStart + 2;
		}
		builder.append(template.substring(templateStart));

		// if we run out of placeholders, append the extra args in square braces
		if (i < args.length) {
			builder.append(" [");
			builder.append(args[i++]);
			while (i < args.length) {
				builder.append(", ");
				builder.append(args[i++]);
			}
			builder.append(']');
		}

		return builder.toString();
	}

	/**
	 * Ensures the truth of an expression involving one or more parameters to
	 * the calling method.
	 *
	 * (Copied from Guava Preconditions)
	 * 
	 * @param expression
	 *            a boolean expression
	 * @throws IllegalArgumentException
	 *             if {@code expression} is false
	 * @since 0.1
	 */
	public static void checkArgument(boolean expression) {
		if (!expression) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Ensures the truth of an expression involving one or more parameters to
	 * the calling method.
	 *
	 * (Copied from Guava Preconditions)
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param errorMessage
	 *            the exception message to use if the check fails; will be
	 *            converted to a string using {@link String#valueOf(Object)}
	 * @throws IllegalArgumentException
	 *             if {@code expression} is false
	 * @since 0.1
	 */
	public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
		if (!expression) {
			throw new IllegalArgumentException(String.valueOf(errorMessage));
		}
	}

	/**
	 * Ensures the truth of an expression involving one or more parameters to
	 * the calling method.
	 *
	 * (Copied from Guava Preconditions)
	 *
	 * @param expression
	 *            a boolean expression
	 * @param errorMessageTemplate
	 *            a template for the exception message should the check fail.
	 *            The message is formed by replacing each {@code %s} placeholder
	 *            in the template with an argument. These are matched by
	 *            position - the first {@code %s} gets {@code
	 *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to
	 *            the formatted message in square braces. Unmatched placeholders
	 *            will be left as-is.
	 * @param errorMessageArgs
	 *            the arguments to be substituted into the message template.
	 *            Arguments are converted to strings using
	 *            {@link String#valueOf(Object)}.
	 * @throws IllegalArgumentException
	 *             if {@code expression} is false
	 * @throws NullPointerException
	 *             if the check fails and either {@code errorMessageTemplate} or
	 *             {@code errorMessageArgs} is null (don't let this happen)
	 * @since 0.1
	 */
	public static void checkArgument(boolean expression, @Nullable String errorMessageTemplate,
			@Nullable Object... errorMessageArgs) {
		if (!expression) {
			throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
		}
	}

	/**
	 * Ensures that an object reference passed as a parameter to the calling
	 * method is not null. (Copied from Guava)
	 * 
	 * @param reference
	 *            an object reference
	 * @return the non-null reference that was validated
	 * @throws NullPointerException
	 *             if {@code reference} is null
	 * @since 0.1
	 */
	public static <T> T checkNotNull(T reference) {
		if (reference == null) {
			throw new NullPointerException();
		}
		return reference;
	}

	/**
	 * Ensures that an object reference passed as a parameter to the calling
	 * method is not null.
	 * 
	 * (Copied from Guava)
	 * 
	 * @param reference
	 *            an object reference
	 * @param errorMessage
	 *            the exception message to use if the check fails; will be
	 *            converted to a string using {@link String#valueOf(Object)}
	 * @return the non-null reference that was validated
	 * @throws NullPointerException
	 *             if {@code reference} is null
	 * @since 0.1
	 */
	public static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
		if (reference == null) {
			throw new NullPointerException(String.valueOf(errorMessage));
		}
		return reference;
	}

	/**
	 * 
	 * 
	 * Saves a LexicalResource complete with all the lexicons, synsets, etc into
	 * a database. This method is suitable only for small lexical resources and
	 * generally for testing purposes. If you have a big resource, stream the loading by 
	 * providing your implementation of <a href=
	 * "https://github.com/dkpro/dkpro-uby/blob/master/de.tudarmstadt.ukp.uby.persistence.transform-asl/src/main/java/de/tudarmstadt/ukp/lmf/transform/LMFDBTransformer.java"
	 * target="_blank"> LMFDBTransformer</a> and call {@code transform()} on it
	 * instead.
	 * 
	 * @param lexicalResourceId
	 *            todo don't know well the meaning
	 * 
	 * @throws SmuException
	 * @since 0.1
	 */
	public static void saveLexicalResourceToDb(DBConfig dbConfig, LexicalResource lexicalResource,
			String lexicalResourceId) {
		try {
			new JavaToDbTransformer(dbConfig, lexicalResource, lexicalResourceId).transform();
		} catch (Exception ex) {
			throw new SmuException("Something went wrong when importing lexical resource " + lexicalResourceId + " !",
					ex);
		}
	}
}
