package com.brewery.app.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;

@Getter
public abstract class Auditable {
    @Id
    protected String id;
    @Version
    private Integer version;
    @CreatedDate
    private Long createdOn;
    @LastModifiedDate
    private Long updatedOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String modifiedBy;
    @Setter(AccessLevel.PACKAGE)
    private String tenantId;
}
