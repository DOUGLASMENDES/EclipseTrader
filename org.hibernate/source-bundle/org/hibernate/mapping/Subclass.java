//$Id: Subclass.java 10119 2006-07-14 00:09:19Z steve.ebersole@jboss.com $
package org.hibernate.mapping;

import java.util.*;
import java.util.Map;

import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.util.JoinedIterator;
import org.hibernate.util.SingletonIterator;

/**
 * A sublass in a table-per-class-hierarchy mapping
 * @author Gavin King
 */
public class Subclass extends PersistentClass {

	private PersistentClass superclass;
	private Class classPersisterClass;
	private final int subclassId;
	
	public Subclass(PersistentClass superclass) {
		this.superclass = superclass;
		this.subclassId = superclass.nextSubclassId();
	}

	int nextSubclassId() {
		return getSuperclass().nextSubclassId();
	}
	
	public int getSubclassId() {
		return subclassId;
	}
	
	public String getCacheConcurrencyStrategy() {
		return getSuperclass().getCacheConcurrencyStrategy();
	}

	public RootClass getRootClass() {
		return getSuperclass().getRootClass();
	}

	public PersistentClass getSuperclass() {
		return superclass;
	}

	public Property getIdentifierProperty() {
		return getSuperclass().getIdentifierProperty();
	}
	public KeyValue getIdentifier() {
		return getSuperclass().getIdentifier();
	}
	public boolean hasIdentifierProperty() {
		return getSuperclass().hasIdentifierProperty();
	}
	public Value getDiscriminator() {
		return getSuperclass().getDiscriminator();
	}
	public boolean isMutable() {
		return getSuperclass().isMutable();
	}
	public boolean isInherited() {
		return true;
	}
	public boolean isPolymorphic() {
		return true;
	}

	public void addProperty(Property p) {
		super.addProperty(p);
		getSuperclass().addSubclassProperty(p);
	}
	public void addJoin(Join j) {
		super.addJoin(j);
		getSuperclass().addSubclassJoin(j);
	}

	public Iterator getPropertyClosureIterator() {
		return new JoinedIterator(
				getSuperclass().getPropertyClosureIterator(),
				getPropertyIterator()
			);
	}
	public Iterator getTableClosureIterator() {
		return new JoinedIterator(
				getSuperclass().getTableClosureIterator(),
				new SingletonIterator( getTable() )
			);
	}
	public Iterator getKeyClosureIterator() {
		return new JoinedIterator(
				getSuperclass().getKeyClosureIterator(),
				new SingletonIterator( getKey() )
			);
	}
	protected void addSubclassProperty(Property p) {
		super.addSubclassProperty(p);
		getSuperclass().addSubclassProperty(p);
	}
	protected void addSubclassJoin(Join j) {
		super.addSubclassJoin(j);
		getSuperclass().addSubclassJoin(j);
	}

	protected void addSubclassTable(Table table) {
		super.addSubclassTable(table);
		getSuperclass().addSubclassTable(table);
	}

	public boolean isVersioned() {
		return getSuperclass().isVersioned();
	}
	public Property getVersion() {
		return getSuperclass().getVersion();
	}

	public boolean hasEmbeddedIdentifier() {
		return getSuperclass().hasEmbeddedIdentifier();
	}
	public Class getEntityPersisterClass() {
		if (classPersisterClass==null) {
			return getSuperclass().getEntityPersisterClass();
		}
		else {
			return classPersisterClass;
		}
	}

	public Table getRootTable() {
		return getSuperclass().getRootTable();
	}

	public KeyValue getKey() {
		return getSuperclass().getIdentifier();
	}

	public boolean isExplicitPolymorphism() {
		return getSuperclass().isExplicitPolymorphism();
	}

	public void setSuperclass(PersistentClass superclass) {
		this.superclass = superclass;
	}

	public String getWhere() {
		return getSuperclass().getWhere();
	}

	public boolean isJoinedSubclass() {
		return getTable()!=getRootTable();
	}

	public void createForeignKey() {
		if ( !isJoinedSubclass() ) {
			throw new AssertionFailure( "not a joined-subclass" );
		}
		getKey().createForeignKeyOfEntity( getSuperclass().getEntityName() );
	}

	public void setEntityPersisterClass(Class classPersisterClass) {
		this.classPersisterClass = classPersisterClass;
	}

	public boolean isLazyPropertiesCacheable() {
		return getSuperclass().isLazyPropertiesCacheable();
	}

	public int getJoinClosureSpan() {
		return getSuperclass().getJoinClosureSpan() + super.getJoinClosureSpan();
	}

	public int getPropertyClosureSpan() {
		return getSuperclass().getPropertyClosureSpan() + super.getPropertyClosureSpan();
	}

	public Iterator getJoinClosureIterator() {
		return new JoinedIterator(
			getSuperclass().getJoinClosureIterator(),
			super.getJoinClosureIterator()
		);
	}

	public boolean isClassOrSuperclassJoin(Join join) {
		return super.isClassOrSuperclassJoin(join) || getSuperclass().isClassOrSuperclassJoin(join);
	}

	public boolean isClassOrSuperclassTable(Table table) {
		return super.isClassOrSuperclassTable(table) || getSuperclass().isClassOrSuperclassTable(table);
	}

	public Table getTable() {
		return getSuperclass().getTable();
	}

	public boolean isForceDiscriminator() {
		return getSuperclass().isForceDiscriminator();
	}

	public boolean isDiscriminatorInsertable() {
		return getSuperclass().isDiscriminatorInsertable();
	}

	public java.util.Set getSynchronizedTables() {
		HashSet result = new HashSet();
		result.addAll(synchronizedTables);
		result.addAll( getSuperclass().getSynchronizedTables() );
		return result;
	}

	public Object accept(PersistentClassVisitor mv) {
		return mv.accept(this);
	}

	public Map getFilterMap() {
		return getSuperclass().getFilterMap();
	}

	public boolean hasSubselectLoadableCollections() {
		return super.hasSubselectLoadableCollections() || 
			getSuperclass().hasSubselectLoadableCollections();
	}

	public String getTuplizerImplClassName(EntityMode mode) {
		String impl = super.getTuplizerImplClassName( mode );
		if ( impl == null ) {
			impl = getSuperclass().getTuplizerImplClassName( mode );
		}
		return impl;
	}

	public Map getTuplizerMap() {
		Map specificTuplizerDefs = super.getTuplizerMap();
		Map superclassTuplizerDefs = getSuperclass().getTuplizerMap();
		if ( specificTuplizerDefs == null && superclassTuplizerDefs == null ) {
			return null;
		}
		else {
			Map combined = new HashMap();
			if ( superclassTuplizerDefs != null ) {
				combined.putAll( superclassTuplizerDefs );
			}
			if ( specificTuplizerDefs != null ) {
				combined.putAll( specificTuplizerDefs );
			}
			return java.util.Collections.unmodifiableMap( combined );
		}
	}

	public Component getIdentifierMapper() {
		return superclass.getIdentifierMapper();
	}
	
	public int getOptimisticLockMode() {
		return superclass.getOptimisticLockMode();
	}

}
