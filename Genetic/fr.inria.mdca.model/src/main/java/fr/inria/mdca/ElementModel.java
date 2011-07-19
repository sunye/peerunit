/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package fr.inria.mdca;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Element Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link fr.inria.mdca.ElementModel#getVariables <em>Variables</em>}</li>
 * </ul>
 * </p>
 *
 * @see fr.inria.mdca.MdcaPackage#getElementModel()
 * @model
 * @generated
 */
public interface ElementModel extends EObject {
	/**
	 * Returns the value of the '<em><b>Variables</b></em>' reference list.
	 * The list contents are of type {@link fr.inria.mdca.Variable}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Variables</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Variables</em>' reference list.
	 * @see fr.inria.mdca.MdcaPackage#getElementModel_Variables()
	 * @model
	 * @generated
	 */
	EList<Variable> getVariables();

} // ElementModel
