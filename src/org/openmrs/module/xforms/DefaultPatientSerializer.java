package org.openmrs.module.xforms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonName;

//TODO This class may need to be refactored out of the XForms module. Am not very sure for now.

/**
 * Provides default serialization of patients to clients. This class is used
 * when sending a list of patients to clients like mobile devices that may want
 * to collect patient data in for instance offline mode.
 * 
 * For those who want a different serialization format for patients, just
 * implement the SerializableData interface and specify the class using the
 * openmrs global property {xforms.patientSerializer}. The jar containing this
 * class can then be put under the webapps/openmrs/web-inf/lib folder. One of
 * the reasons one could want a different serialization format is for
 * performance by doing a more optimized and compact format. Onother reason i
 * can foresee is for non java clients who may say want the patients in xml or
 * any other format, because the default implementation assumes java clients.
 * 
 * @author Daniel
 * 
 */
public class DefaultPatientSerializer {

	private Log log = LogFactory.getLog(this.getClass());

	public DefaultPatientSerializer() {

	}

	/**
	 * Serializes a patient to the stream.
	 * 
	 * @param patient -
	 *            the patient to serialize.
	 * @param dos -
	 *            the stream to write to.
	 */
	protected void serialize(Patient patient, DataOutputStream dos) {
		try {
			SerializationUtils.writeInteger(dos, patient.getPatientId());

			PersonName personName = patient.getPersonName();
			SerializationUtils.writeUTF(dos, personName.getPrefix());
			SerializationUtils.writeUTF(dos, personName.getFamilyName());
			SerializationUtils.writeUTF(dos, personName.getMiddleName());
			SerializationUtils.writeUTF(dos, personName.getGivenName());
			SerializationUtils.writeUTF(dos, patient.getGender());
			SerializationUtils.writeDate(dos, patient.getBirthdate());

			if (patient.getPatientIdentifier() != null)
				SerializationUtils.writeUTF(dos, patient.getPatientIdentifier()
						.toString());
			else
				dos.writeBoolean(false);

			dos.writeBoolean(false); // flag to tell whether this is a new
										// patient or not.
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * @see org.openmrs.module.xforms.SerializableData#serialize(java.io.DataOutputStream,
	 *      java.lang.Object)
	 */
	public void serialize(DataOutputStream dos, Object data) {
		try {
			if (data == null)
				return;
			PatientData patientData = (PatientData) data; // data will always
															// be a patient
															// data.

			List<Patient> patients = patientData.getPatients();
			if (patients == null || patients.size() == 0)
				return;

			dos.writeInt(patients.size());
			for (Patient patient : patients)
				serialize(patient, dos);

			List<PatientTableField> fields = patientData.getFields();
			if (fields == null || fields.size() == 0)
				return;
			dos.writeByte(fields.size());
			for (PatientTableField field : fields) {
				dos.writeInt(field.getId());
				dos.writeUTF(field.getName());
			}

			List<PatientTableFieldValue> fieldVals = patientData
					.getFieldValues();
			if (fieldVals == null || fieldVals.size() == 0)
				return;
			dos.writeInt(fieldVals.size());
			for (PatientTableFieldValue fieldVal : fieldVals) {
				dos.writeInt(fieldVal.getFieldId());
				dos.writeInt(fieldVal.getPatientId());
				dos.writeUTF(fieldVal.getValue().toString());
			}

		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}
	}

	/**
	 * @see org.openmrs.module.xforms.SerializableData#deSerialize(java.io.DataInputStream,
	 *      java.lang.Object)
	 */
	public Object deSerialize(DataInputStream dis, Object data) {
		return null; // not necessary for now because patients come back as
						// xforms.
	}
}
