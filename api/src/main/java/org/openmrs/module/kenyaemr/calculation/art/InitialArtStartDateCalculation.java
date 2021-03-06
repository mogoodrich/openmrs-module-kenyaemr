/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyaemr.calculation.art;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.calculation.BaseEmrCalculation;
import org.openmrs.module.kenyaemr.calculation.CalculationUtils;

/**
 * Calculates the date on which a patient first started ART
 */
public class InitialArtStartDateCalculation extends BaseEmrCalculation {
	
	/**
	 * @see org.openmrs.module.kenyaemr.calculation.BaseEmrCalculation#getName()
	 */
	@Override
	public String getName() {
		return "Initial ART Start Date";
	}

	@Override
	public String[] getTags() {
		return new String[] { "hiv" };
	}
	
	/**
	 * @see org.openmrs.calculation.patient.PatientCalculation#evaluate(java.util.Collection, java.util.Map, org.openmrs.calculation.patient.PatientCalculationContext)
	 * @should return null for patients who have not started ART
	 * @should return start date for patients who have started ART
	 */
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues,
	                                     PatientCalculationContext context) {

		// Get earliest dates from orders
		Concept arvs = getConcept(Dictionary.ANTIRETROVIRAL_DRUGS);
		CalculationResultMap earliestOrderDates = earliestStartDates(allDrugOrders(arvs, cohort, context), context);

		// Return the earliest of the two
		CalculationResultMap result = new CalculationResultMap();
		for (Integer ptId : cohort) {
			Date orderDate = CalculationUtils.datetimeResultForPatient(earliestOrderDates, ptId);

			result.put(ptId, orderDate == null ? null : new SimpleResult(orderDate, null));
		}
		return result;
	}
}