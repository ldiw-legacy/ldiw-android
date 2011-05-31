package com.letsdoitworld.wastemapper;

import java.util.HashMap;

public final class SharedPool {

  private static SharedPool sharedPool = null;
  private volatile HashMap<String, Object> hashmap;
  private volatile boolean canRead;

  private SharedPool() {
    hashmap = new HashMap<String, Object>();
    canRead = true;
  }

  synchronized public static SharedPool getInstance() {
    if (sharedPool == null) {
      sharedPool = new SharedPool();
    }
    return sharedPool;
  }

  synchronized public Object put(String name, Object object) {
    while (!canRead) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
    canRead = false;
    Object object1 = hashmap.put(name, object);
    canRead = true;
    notify();
    return object1;
  }

  synchronized public Object get(String name) {
    while (!canRead) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
    canRead = false;
    Object object = hashmap.get(name);
    canRead = true;
    notify();
    return object;
  }

  synchronized public boolean containsKey(String name) {
    while (!canRead) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
    canRead = false;
    boolean bool = hashmap.containsKey(name);
    canRead = true;
    notify();
    return bool;
  }

  synchronized public Object remove(String name) {
    while (!canRead) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
    canRead = false;
    Object object = hashmap.remove(name);
    canRead = true;
    notify();
    return object;
  }

}
