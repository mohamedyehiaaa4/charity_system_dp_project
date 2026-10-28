package com.fooddonation.model.lookup;

public class LookupEntry {
    private long id;
    private LookupType type;
    private String name;
    private boolean system;

    public LookupEntry() {
    }

    protected LookupEntry(LookupType type) {
        this.type = type;
    }

    public LookupEntry(long id, LookupType type, String name, boolean system) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.system = system;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LookupType getType() {
        return type;
    }

    public void setType(LookupType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** System rows are protected: they can't be renamed or deleted. */
    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }
}
