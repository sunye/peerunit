/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package fr.inria.mdca;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Variable</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link fr.inria.mdca.Variable#getUpperbound <em>Upperbound</em>}</li>
 *   <li>{@link fr.inria.mdca.Variable#getValues <em>Values</em>}</li>
 * </ul>
 * </p>
 *
 * @see fr.inria.mdca.MdcaPackage#getVariable()
 * @model
 * @generated
 */
public interface Variable extends NamedElement {
	/**
	 * Returns the value of the '<em><b>Upperbound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Upperbound</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Upperbound</em>' attribute.
	 * @see #setUpperbound(int)
	 * @see fr.inria.mdca.MdcaPackage#getVariable_Upperbound()
	 * @model
	 * @generated
	 */
	int getUpperbound();

	/**
	 * Sets the value of the '{@link fr.inria.mdca.Variable#getUpperbound <em>Upperbound</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Upperbound</em>' attribute.
	 * @see #getUpperbound()
	 * @generated
	 */
	void setUpperbound(int value);

	/**
	 * Returns the value of the '<em><b>Values</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Values</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Values</em>' attribute list.
	 * @see fr.inria.mdca.MdcaPackage#getVariable_Values()
	 * @model
	 * @generated
	 */
	EList<String> getValues();

} // Variable
