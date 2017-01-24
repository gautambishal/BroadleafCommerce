/*
 * #%L
 * BroadleafCommerce Open Admin Platform
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.openadmin.server.service.persistence.module;

import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.presentation.client.OperationType;
import org.broadleafcommerce.openadmin.dto.BasicFieldMetadata;
import org.broadleafcommerce.openadmin.dto.CriteriaTransferObject;
import org.broadleafcommerce.openadmin.dto.DynamicResultSet;
import org.broadleafcommerce.openadmin.dto.Entity;
import org.broadleafcommerce.openadmin.dto.EntityResult;
import org.broadleafcommerce.openadmin.dto.FieldMetadata;
import org.broadleafcommerce.openadmin.dto.PersistencePackage;
import org.broadleafcommerce.openadmin.dto.PersistencePerspective;
import org.broadleafcommerce.openadmin.dto.Property;
import org.broadleafcommerce.openadmin.server.dao.DynamicEntityDao;
import org.broadleafcommerce.openadmin.server.service.ValidationException;
import org.broadleafcommerce.openadmin.server.service.persistence.module.criteria.FilterMapping;
import org.broadleafcommerce.openadmin.server.service.persistence.module.criteria.RestrictionFactory;
import org.broadleafcommerce.openadmin.server.service.persistence.validation.EntityValidatorService;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Helper interface for serializing/deserializing the generic {@link Entity} DTO to/from its actual domain object
 * representation. 
 * 
 * @author jfischer
 * @see {@link BasicPersistenceModule}
 * @see {@link MapStructurePersistenceModule}
 * @see {@link AdornedTargetListPersistenceModule}
 */
public interface RecordHelper extends DataFormatProvider {

    List<FilterMapping> getFilterMappings(PersistencePerspective persistencePerspective, CriteriaTransferObject cto,
                                                 String ceilingEntityFullyQualifiedClassname,
                                                 Map<String, FieldMetadata> mergedProperties);

    List<FilterMapping> getFilterMappings(PersistencePerspective persistencePerspective, CriteriaTransferObject cto,
                                                     String ceilingEntityFullyQualifiedClassname,
                                                     Map<String, FieldMetadata> mergedUnfilteredProperties,
                                                     RestrictionFactory customRestrictionFactory);

    Entity[] getRecords(Map<String, FieldMetadata> primaryMergedProperties, List<? extends Serializable> records,
                                   Map<String, FieldMetadata> alternateMergedProperties, String pathToTargetObject);

    /**
     * 
     * @param primaryMergedProperties
     * @param records
     * @param alternateMergedProperties
     * @param pathToTargetObject
     * @param customCriteria
     * @deprecated use getRefinedRecords with persistencePackage parameter
     * @return
     */
    @Deprecated
    Entity[] getRecords(Map<String, FieldMetadata> primaryMergedProperties, List<? extends Serializable> records,
                               Map<String, FieldMetadata> alternateMergedProperties, String pathToTargetObject,
                               String[] customCriteria);

    /**
     * Refines (if a list grid fetch) and retrieves the entity records for the passed in properties
     * 
     * @param primaryMergedProperties
     * @param records
     * @param alternateMergedProperties
     * @param pathToTargetObject
     * @param persistencePackage
     * @return
     */
    Entity[] getRefinedRecords(Map<String, FieldMetadata> primaryMergedProperties, List<? extends Serializable> records,
                        Map<String, FieldMetadata> alternateMergedProperties, String pathToTargetObject,
                        PersistencePackage persistencePackage);

    Entity[] getRecords(Map<String, FieldMetadata> primaryMergedProperties, List<? extends Serializable> records);
    
    Entity[] getRecords(Class<?> ceilingEntityClass, PersistencePerspective persistencePerspective, List<? extends Serializable> records);
    
    Entity getRecord(Map<String, FieldMetadata> primaryMergedProperties, Serializable record, Map<String, FieldMetadata> alternateMergedProperties, String pathToTargetObject);
    
    Entity getRecord(Class<?> ceilingEntityClass, PersistencePerspective persistencePerspective, Serializable record);

    /**
     * <p>Populates a Hibernate entity <b>instance</b> based on the values from <b>entity</b> (the DTO representation of
     * <b>instance</b>) and the metadata from <b>mergedProperties</b>.</p>
     * <p>While populating <b>instance</b>, validation is also performed using the {@link EntityValidatorService}. If this
     * validation fails, then the instance is left unchanged and a {@link ValidationExcpetion} is thrown. In the common
     * case, this exception bubbles up to the {@link DynamicRemoteService} which catches the exception and communicates
     * appropriately to the invoker</p>
     * 
     * @param instance
     * @param entity
     * @param mergedProperties
     * @param setId
     * @param validateUnsubmittedProperties if set to true, will ignore validation for properties that weren't submitted
     *                                      along with the entity
     * @throws ValidationException if after populating <b>instance</b> via the values in <b>entity</b> then
     * {@link EntityValidatorService#validate(Entity, Serializable, Map)} returns false
     * @return <b>instance</b> populated with the property values from <b>entity</b> according to the metadata specified
     * in <b>mergedProperties</b>
     * @see {@link EntityValidatorService}
     */
    Serializable createPopulatedInstance(Serializable instance, Entity entity,
            Map<String, FieldMetadata> mergedProperties, Boolean setId, Boolean validateUnsubmittedProperties) throws ValidationException;

    /**
     * Delegates to the overloaded method with validateUnsubmittedProperties set to true.
     * 
     * @see #createPopulatedInstance(Serializable, Entity, Map, Boolean, Boolean)
     */
    Serializable createPopulatedInstance(Serializable instance, Entity entity,
            Map<String, FieldMetadata> unfilteredProperties, Boolean setId) throws ValidationException;
    
    Object getPrimaryKey(Entity entity, Map<String, FieldMetadata> mergedProperties);
    
    Map<String, FieldMetadata> getSimpleMergedProperties(String entityName, PersistencePerspective persistencePerspective);
    
    FieldManager getFieldManager();

    PersistenceModule getCompatibleModule(OperationType operationType);

    /**
     * Validates the {@link Entity} based on the validators associated with each property
     * @param entity the instance that is attempted to be saved from. Implementers should set {@link Entity#isValidationFailure()}
     * accordingly as a result of the validation
     * @param populatedInstance
     * @param mergedProperties
     * @param validateUnsubmittedProperties if set to true, will ignore validation for properties that weren't submitted
     *                                      along with the entity
     * @return whether or not the entity passed validation. This yields the same result as calling !{@link Entity#isValidationFailure()}
     * after invoking this method
     */
    boolean validate(Entity entity, Serializable populatedInstance, Map<String, FieldMetadata> mergedProperties, boolean validateUnsubmittedProperties);

    /**
     * Delegates to the overloaded method with validateUnsubmittedProperties set to true.
     * 
     * @see #validate(Entity, Serializable, Map, boolean)
     */
    boolean validate(Entity entity, Serializable populatedInstance, Map<String, FieldMetadata> mergedProperties);

    Map<String, FieldMetadata> getFilteredProperties(PersistencePackage persistencePackage, CriteriaTransferObject cto) throws ServiceException;

    void decorateProperty(Property property, String propertyValueString, BasicFieldMetadata fieldMetadata);

    Integer getTotalRecords(String ceilingEntity, List<FilterMapping> filterMappings);

    Serializable getMaxValue(String ceilingEntity, List<FilterMapping> filterMappings, String maxField);

    List<Serializable> getPersistentRecords(String ceilingEntity, List<FilterMapping> filterMappings, Integer firstResult, Integer maxResults);

    EntityResult update(PersistencePackage persistencePackage, boolean includeRealEntityObject) throws ServiceException;

    EntityResult add(PersistencePackage persistencePackage, boolean includeRealEntityObject) throws ServiceException;

    /**
     * Returns a string representation of the field on the given instance specified by the property name. The propertyName
     * should start from the root of the given instance
     * 
     * @param instance
     * @param propertyName
     * @return
     */
    String getStringValueFromGetter(Serializable instance, String propertyName)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

}
