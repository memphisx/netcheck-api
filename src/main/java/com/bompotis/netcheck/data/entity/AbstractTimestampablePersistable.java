package com.bompotis.netcheck.data.entity;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ClassUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 16/6/20.
 */
@MappedSuperclass
public abstract class AbstractTimestampablePersistable<PK extends Serializable> implements Persistable<PK> {

    @Id
    @GeneratedValue(generator="uuid")
    @GenericGenerator(name="uuid", strategy = "uuid2")
    @Column(name = "id")
    private PK id;

    @CreatedDate
    @Column(name = "created_at")
    private Date createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Date updatedAt;

    protected AbstractTimestampablePersistable() {}

    public PK getId() {
        return this.id;
    }

    @Transient
    public boolean isNew() {
        return null == this.getId();
    }

    public Date getCreatedAt() {
        return null == createdAt ? null : createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void prePersist() {
        if (this.createdAt == null) createdAt = new Date();
        if (this.updatedAt == null) updatedAt = new Date();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = new Date();
    }

    @PreRemove
    protected void preRemove() {
        this.updatedAt = new Date();
    }

    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), this.getId());
    }

    public boolean equals(Object obj) {
        if(null == obj) {
            return false;
        } else if(this == obj) {
            return true;
        } else if(!this.getClass().equals(ClassUtils.getUserClass(obj))) {
            return false;
        } else {
            AbstractTimestampablePersistable that = (AbstractTimestampablePersistable)obj;
            return null != this.getId() && this.getId().equals(that.getId());
        }
    }

    public int hashCode() {
        byte hashCode = 17;
        return hashCode + ((null == this.getId()) ? 0 : (this.getId().hashCode() * 31));
    }
}