package com.naveen.objectstore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.mule.api.store.ListableObjectStore;
import org.mule.api.store.ObjectStoreException;
import org.mule.util.store.AbstractMonitoredObjectStore;

public class CustomMonitoredObjectStore<T extends Serializable>
  extends AbstractMonitoredObjectStore<T>
{
  private static final Logger logger = Logger.getLogger(CustomMonitoredObjectStore.class);
  protected ListableObjectStore<T> chObjectStore = null;
  private static String OS_TIME = "ostimestamp";
  private static String VALUE = "value";
  
  CustomMonitoredObjectStore(ListableObjectStore<T> objectStore)
  {
    this.chObjectStore = objectStore;
    logger.info("Initializing CustomMonitoredObjectStore with objectstore " + objectStore);
  }
  
  public boolean contains(Serializable key)
    throws ObjectStoreException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("contains - key=" + key);
    }
    return this.chObjectStore.contains(key);
  }
  
  public void store(Serializable key, T value)
    throws ObjectStoreException
  {
    Map<String, Object> map = new HashMap<>();
    map.put(VALUE, value);
    map.put(OS_TIME, Long.valueOf(System.nanoTime()));
    if (logger.isDebugEnabled()) {
      logger.debug("store - key=" + key + "; value=" + map);
    }
    this.chObjectStore.store((Serializable)key, (T)map);
  }
  
  public T retrieve(Serializable key)
    throws ObjectStoreException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("retrieve - key=" + key);
    }
    T value = null;
    value = this.chObjectStore.retrieve(key);
    if ((value != null) && ((value instanceof Map)))
    {
      Map<String, Object> map = (Map<String, Object>)value;
      if ((map.keySet().contains(VALUE)) && (map.keySet().contains(OS_TIME))) {
        value = (T)map.get(VALUE);
      }
    }
    return value;
  }
  
  public T retrieveValue(Serializable key)
    throws ObjectStoreException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("retrieve - key=" + key);
    }
    return this.chObjectStore.retrieve(key);
  }
  
  public T remove(Serializable key)
    throws ObjectStoreException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("remove - key=" + key);
    }
    T value = null;
    value = this.chObjectStore.remove(key);
    if ((value != null) && ((value instanceof Map)))
    {
      Map<String, Object> map = (Map<String, Object>)value;
      if ((map.keySet().contains(VALUE)) && (map.keySet().contains(OS_TIME))) {
        value = (T)map.get(VALUE);
      }
    }
    return value;
  }
  
  public boolean isPersistent()
  {
    if (logger.isDebugEnabled()) {
      logger.debug("isPersistent : " + this.chObjectStore.isPersistent());
    }
    return this.chObjectStore.isPersistent();
  }
  
  public void clear()
    throws ObjectStoreException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Clearing objectstore - " + this.chObjectStore);
    }
    this.chObjectStore.clear();
  }
  
  protected void expire()
  {
    if (logger.isDebugEnabled()) {
      logger.debug("expire - entryTTL=" + this.entryTTL + ", expirationInterval=" + this.expirationInterval);
    }
    int expiredEntries = 0;
    try
    {
      int currentSize = 0;
      List<Serializable> keys = this.chObjectStore.allKeys();
      if (keys != null) {
        currentSize = keys.size();
      }
      if ((this.entryTTL > 0) && (currentSize != 0))
      {
        long now = System.nanoTime();
        T value = null;
        for (Serializable key : keys)
        {
          value = retrieveValue(key);
          if (logger.isTraceEnabled()) {
            logger.trace("Retrieved entry - key=" + key + "; value=" + value);
          }
          if ((value != null) && ((value instanceof Map)))
          {
            Map<String, Object> map = (Map<String, Object>)value;
            if ((map.keySet().contains(VALUE)) && (map.keySet().contains(OS_TIME)))
            {
              long entryTimeValue = ((Long)map.get(OS_TIME)).longValue();
              if (logger.isTraceEnabled()) {
                logger.trace("key=:" + key + ",entryTimeValue =" + entryTimeValue + ",now=" + now + ",difference =" + TimeUnit.NANOSECONDS.toMillis(now - entryTimeValue));
              }
              if (TimeUnit.NANOSECONDS.toMillis(now - entryTimeValue) >= this.entryTTL)
              {
                if (logger.isDebugEnabled()) {
                  logger.debug("Removing expired entry from objectstore - " + key);
                }
                remove(key);
                expiredEntries++;
              }
            }
          }
        }
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Expired " + expiredEntries + " old entries");
      }
    }
    catch (Exception e)
    {
      if (e.getMessage().equals("No partition named: DEFAULT_PARTITION")) {
        logger.warn("Error occured during CustomCHObjectStore.expire() - " + e.getMessage());
      } else {
        logger.error("Error occured during CustomCHObjectStore.expire() - " + e.getMessage());
      }
    }
  }
}
