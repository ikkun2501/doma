/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.jdbc.entity;

import java.util.function.Supplier;

import org.seasar.doma.jdbc.domain.DomainType;
import org.seasar.doma.wrapper.NumberWrapper;
import org.seasar.doma.wrapper.NumberWrapperVisitor;
import org.seasar.doma.wrapper.Wrapper;

/**
 * バージョンのプロパティ型です。
 * 
 * @author nakamura-to
 * 
 * @param <PARENT>
 * @param <ENTITY>
 * @param <PROPERTY>
 * @param <BASIC>
 * @param <DOMAIN>
 */
public class VersionPropertyType<PARENT, ENTITY extends PARENT, BASIC extends Number, DOMAIN>
        extends DefaultPropertyType<PARENT, ENTITY, BASIC, DOMAIN> {

    /**
     * インスタンスを構築します。
     * 
     * @param entityClass
     *            エンティティのクラス
     * @param entityPropertyClass
     *            プロパティのクラス
     * @param wrapperSupplier
     *            ラッパーのサプライヤ
     * @param parentEntityPropertyType
     *            親のエンティティのプロパティ型、親のエンティティを持たない場合 {@code null}
     * @param domainType
     *            ドメインのメタタイプ、ドメインでない場合 {@code null}
     * @param name
     *            プロパティの名前
     * @param columnName
     *            カラム名
     */
    public VersionPropertyType(Class<ENTITY> entityClass,
            Class<?> entityPropertyClass, Class<BASIC> basicClass,
            Supplier<Wrapper<BASIC>> wrapperSupplier,
            EntityPropertyType<PARENT, BASIC> parentEntityPropertyType,
            DomainType<BASIC, DOMAIN> domainType, String name, String columnName) {
        super(entityClass, entityPropertyClass, basicClass, wrapperSupplier,
                parentEntityPropertyType, domainType, name, columnName, true,
                true);
    }

    @Override
    public boolean isVersion() {
        return true;
    }

    /**
     * 必要であればバージョンの値を設定します。
     * 
     * @param entityType
     *            エンティティのタイプ
     * @param entity
     *            エンティティ
     * @param value
     *            バージョンの値
     * @return エンティティ
     */
    public ENTITY setIfNecessary(EntityType<ENTITY> entityType, ENTITY entity,
            Number value) {
        return modify(entityType, entity, new ValueSetter(), value);
    }

    /**
     * バージョン番号をインクリメントします。
     * 
     * @param entityType
     *            エンティティのタイプ
     * @param entity
     *            エンティティ
     * @return エンティティ
     */
    public ENTITY increment(EntityType<ENTITY> entityType, ENTITY entity) {
        return modify(entityType, entity, new Incrementer(), null);
    }

    protected static class ValueSetter implements
            NumberWrapperVisitor<Void, Number, Void, RuntimeException> {

        @Override
        public <V extends Number> Void visitNumberWrapper(
                NumberWrapper<V> wrapper, Number value, Void q) {
            Number currentValue = wrapper.get();
            if (currentValue == null || currentValue.intValue() < 0) {
                wrapper.set(value);
            }
            return null;
        }
    }

    protected static class Incrementer implements
            NumberWrapperVisitor<Void, Void, Void, RuntimeException> {

        @Override
        public <V extends Number> Void visitNumberWrapper(
                NumberWrapper<V> wrapper, Void p, Void q) {
            wrapper.increment();
            return null;
        }
    }

}
