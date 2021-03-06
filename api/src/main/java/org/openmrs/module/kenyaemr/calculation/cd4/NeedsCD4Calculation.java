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

package org.openmrs.module.kenyaemr.calculation.cd4;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ObsResult;
import org.openmrs.module.kenyaemr.Dictionary;
import org.openmrs.module.kenyaemr.KenyaEmrConstants;
import org.openmrs.module.kenyaemr.MetadataConstants;
import org.openmrs.module.kenyaemr.calculation.BaseAlertCalculation;
import org.openmrs.module.kenyaemr.calculation.BooleanResult;
import org.openmrs.module.kenyaemr.calculation.CalculationUtils;

/**
 * Calculate whether patients are due for a CD4 count. Calculation returns true if if the patient
 * is alive, enrolled in the HIV program, and has not had a CD4 count in the last 180 days
 */
public class NeedsCD4Calculation extends BaseAlertCalculation {

	/**
	 * @see org.openmrs.module.kenyaemr.calculation.BaseEmrCalculation#getName()
	 */
	@Override
	public String getName() {
		return "Patients Due for CD4";
	}

	/**
	 * @see org.openmrs.module.kenyaemr.calculation.BaseAlertCalculation#getAlertMessage()
	 */
	@Override
	public String getAlertMessage() {
		return "Due for CD4";
	}

	@Override
	public String[] getTags() {
		return new String[] { "alert", "hiv" };
	}
	
	/**
	 * @see org.openmrs.calculation.patient.PatientCalculation#evaluate(java.util.Collection,
	 *      java.util.Map, org.openmrs.calculation.patient.PatientCalculationContext)
	 * @should determine whether patients need a CD4
	 */
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {

		Program hivProgram = Context.getProgramWorkflowService().getProgramByUuid(MetadataConstants.HIV_PROGRAM_UUID);

		Set<Integer> alive = alivePatients(cohort, context);
		Set<Integer> inHivProgram = CalculationUtils.patientsThatPass(lastProgramEnrollment(hivProgram, alive, context));
		CalculationResultMap lastObs = lastObs(getConcept(Dictionary.CD4_COUNT), cohort, context);

		CalculationResultMap ret = new CalculationResultMap();
		for (Integer ptId : cohort) {
			boolean needsCD4 = false;

			// Is patient alive and in the HIV program
			if (inHivProgram.contains(ptId)) {

				// Does patient have CD4 result in the last X days
				ObsResult r = (ObsResult) lastObs.get(ptId);
				needsCD4 = r == null || r.isEmpty() || daysSince(r.getDateOfResult(), context) > KenyaEmrConstants.NEEDS_CD4_COUNT_AFTER_DAYS;
			}

			ret.put(ptId, new BooleanResult(needsCD4, this, context));
		}
		return ret;
	}
}
