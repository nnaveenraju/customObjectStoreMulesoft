package com.naveen.objectstore;

import java.io.Serializable;

import org.mule.api.store.ObjectStoreException;
import org.mule.util.store.AbstractMonitoredObjectStore;

public class CustomOS<T extends Serializable> extends AbstractMonitoredObjectStore<T> {

	@Override
	public boolean contains(Serializable key) throws ObjectStoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void store(Serializable key, T value) throws ObjectStoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public T retrieve(Serializable key) throws ObjectStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T remove(Serializable key) throws ObjectStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPersistent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() throws ObjectStoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void expire() {
		// TODO Auto-generated method stub
		
	}

}
