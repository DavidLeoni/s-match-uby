package it.unitn.disi.smatch.oracles.uby;

import static it.unitn.disi.smatch.oracles.uby.SmuUtils.checkNotEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;


/**
 * Statistics about edge insertions into the SynsetRelation graph.
 * 
 * @since 0.1
 *
 */
class InsertionStats {
    private Map<String, Long> map;
    
    
    public InsertionStats(){
        this.map = new HashMap();
    }

    public void inc(String key){
        checkNotEmpty(key, "Invalid key!");
        if (map.containsKey(key)){
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1L);
        }
    }

    public Set<String> relNames() {
        return map.keySet();
    }
    
    public long count(String relName){
        Long ret = map.get(relName);
        if (ret == null){
            return 0L;
        } else {
            return ret;
        }
    }

    public long totEdges() {
        long ret = 0;
        for (Long v : map.values()){
            ret += v;
        }
        return ret;
    }

    public void log(Logger log) {
        log.info("");
        long tot = totEdges();
        if (tot == 0){
            log.info("   No edge to insert. ");
        } else {
            log.info("   Inserted " + tot+ " edges:");
            for (String relName : relNames()){
                log.info("        " + relName + ":   " + count(relName) );
            }
        }
        log.info("");
    }    
    
}