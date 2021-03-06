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

package org.openmrs.module.kenyaemr.calculation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.MetadataConstants;

/**
 * Calculates whether patients have taken CTX or Dapsone
 */
public class WithoutCTXOrDapsoneCalculation extends BaseEmrCalculation {

	@Override
	public String getName() {
		return "Patients who have not taken CTX/Dapsone";
	}

	@Override
	public String[] getTags() {
		return new String[] {"hiv"};
	}

	@SuppressWarnings("unchecked")
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {

		Program hivProgram = Context.getProgramWorkflowService().getProgramByUuid(MetadataConstants.HIV_PROGRAM_UUID);

		Set<Integer> alive = alivePatients(cohort, context);
		Set<Integer> inHivProgram = CalculationUtils.patientsThatPass(lastProgramEnrollment(hivProgram, alive, context));

		CalculationResultMap medOrdersObss = allObs(getConcept(Dictionary.MEDICATION_ORDERS), cohort, context);

		// Get concepts for both kinds of medication
		Concept dapsone = getConcept(Dictionary.DAPSONE);
		Concept ctx = getConcept(Dictionary.SULFAMETHOXAZOLE_TRIMETHOPRIM);

		CalculationResultMap ret = new CalculationResultMap();
		for (Integer ptId : cohort) {
			boolean notTakingCtxOrDapsone = false;

			// Is patient alive and in the HIV program
			if (inHivProgram.contains(ptId)) {
				notTakingCtxOrDapsone = true;

				ListResult patientMedOrders = (ListResult) medOrdersObss.get(ptId);
				if (patientMedOrders != null) {
					// Look through list of medication order obs for any Dapsone or CTX
					List<Obs> medOrderObsList = CalculationUtils.extractListResultValues(patientMedOrders);
					for (Obs medOrderObs : medOrderObsList) {
						if (medOrderObs.getValueCoded().equals(dapsone) || medOrderObs.getValueCoded().equals(ctx)) {
							notTakingCtxOrDapsone = false;
							break;
						}
					}
				}
			}

			ret.put(ptId, new BooleanResult(notTakingCtxOrDapsone, this));
		}
		return ret;
	}
}