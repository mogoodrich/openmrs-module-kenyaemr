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
package org.openmrs.module.kenyaemr.page.controller;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.Visit;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.AppUiUtil;
import org.openmrs.module.kenyaemr.MetadataConstants;
import org.openmrs.module.kenyaemr.util.KenyaEmrUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.session.Session;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Medical encounter view patient page
 */
public class MedicalEncounterViewPatientPageController {
	
	public void controller(@RequestParam("patientId") Patient patient,
	                       @RequestParam(value="visitId", required=false) Visit visit,
	                       PageModel model,
	                       Session session) {
		
		AppUiUtil.startApp("kenyaemr.medicalEncounter", session);

		model.addAttribute("patient", patient);
		model.addAttribute("person", patient);

		// Visit to be viewed defaults to first active visit if there is one
		List<Visit> activeVisits = Context.getVisitService().getActiveVisitsByPatient(patient);
		if (visit == null && activeVisits.size() > 0) {
			visit = activeVisits.get(0);
		}
		
		model.addAttribute("visit", visit);

		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		Program hivProgram = pws.getProgramByUuid(MetadataConstants.HIV_PROGRAM_UUID);
		model.addAttribute("hivProgram", hivProgram);

		Program tbProgram = pws.getProgramByUuid(MetadataConstants.TB_PROGRAM_UUID);
		model.addAttribute("tbProgram", tbProgram);

		model.addAttribute("enrolledInHivProgram", KenyaEmrUtils.isPatientInProgram(patient, hivProgram));
		model.addAttribute("enrolledInTbProgram", KenyaEmrUtils.isPatientInProgram(patient, tbProgram));
	}
}