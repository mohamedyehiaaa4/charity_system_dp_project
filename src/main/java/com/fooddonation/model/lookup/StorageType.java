package com.fooddonation.model.lookup;

public class StorageType extends LookupEntry {

    public StorageType() {
        super(LookupType.STORAGE_TYPE);
    }

    public StorageType(long id, String name, boolean system) {
        super(id, LookupType.STORAGE_TYPE, name, system);
    }
}
