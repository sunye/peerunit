/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package fr.inria.mdca.impl;

import fr.inria.mdca.MdcaPackage;
import fr.inria.mdca.Variable;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Variable</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link fr.inria.mdca.impl.VariableImpl#getUpperbound <em>Upperbound</em>}</li>
 *   <li>{@link fr.inria.mdca.impl.VariableImpl#getValues <em>Values</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class VariableImpl extends NamedElementImpl implements Variable {
	/**
	 * The default value of the '{@link #getUpperbound() <em>Upperbound</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUpperbound()
	 * @generated
	 * @ordered
	 */
	protected static final int UPPERBOUND_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getUpperbound() <em>Upperbound</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUpperbound()
	 * @generated
	 * @ordered
	 */
	protected int upperbound = UPPERBOUND_EDEFAULT;

	/**
	 * The cached value of the '{@link #getValues() <em>Values</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValues()
	 * @generated
	 * @ordered
	 */
	protected EList<String> values;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected VariableImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MdcaPackage.Literals.VARIABLE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getUpperbound() {
		return upperbound;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUpperbound(int newUpperbound) {
		int oldUpperbound = upperbound;
		upperbound = newUpperbound;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MdcaPackage.VARIABLE__UPPERBOUND, oldUpperbound, upperbound));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getValues() {
		if (values == null) {
			values = new EDataTypeUniqueEList<String>(String.class, this, MdcaPackage.VARIABLE__VALUES);
		}
		return values;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MdcaPackage.VARIABLE__UPPERBOUND:
				return new Integer(getUpperbound());
			case MdcaPackage.VARIABLE__VALUES:
				return getValues();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case MdcaPackage.VARIABLE__UPPERBOUND:
				setUpperbound(((Integer)newValue).intValue());
				return;
			case MdcaPackage.VARIABLE__VALUES:
				getValues().clear();
				getValues().addAll((Collection<? extends String>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case MdcaPackage.VARIABLE__UPPERBOUND:
				setUpperbound(UPPERBOUND_EDEFAULT);
				return;
			case MdcaPackage.VARIABLE__VALUES:
				getValues().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case MdcaPackage.VARIABLE__UPPERBOUND:
				return upperbound != UPPERBOUND_EDEFAULT;
			case MdcaPackage.VARIABLE__VALUES:
				return values != null && !values.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (upperbound: ");
		result.append(upperbound);
		result.append(", values: ");
		result.append(values);
		result.append(')');
		return result.toString();
	}

} //VariableImpl
