package persistence.sql.entity;

class EntityState {
    private final CacheEntry cacheEntry;
    private Status status;

    EntityState(final Object entity, final Status status) {
        this.cacheEntry = new CacheEntry(entity);
        this.status = status;
    }

    Object getEntity() {
        return cacheEntry.getEntity();
    }

    boolean isDirty() {
        if (status == Status.DELETED || status == Status.GONE) {
            return false;
        }

        if (status == Status.READ_ONLY) {
            return false;
        }

        if (status == Status.SAVING) {
            return true;
        }

        return  cacheEntry.isDirty();
    }

    void makeDeleted() {
        this.status = Status.DELETED;
    }

    Status getStatus() {
        return status;
    }
}
