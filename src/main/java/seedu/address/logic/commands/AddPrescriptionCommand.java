package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONSUMPTION_PER_DAY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DOSAGE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICINE_NAME;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_APPOINTMENTS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.List;

import seedu.address.calendar.GoogleCalendar;
import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.events.ui.PersonPanelSelectionChangedEvent;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentId;
import seedu.address.model.appointment.Prescription;
import seedu.address.model.doctor.Doctor;
import seedu.address.model.patient.Allergy;
import seedu.address.model.patient.Patient;
import seedu.address.model.person.Person;

/**
 * Adds a prescription to an appointment
 */

public class AddPrescriptionCommand extends Command {

    public static final String COMMAND_WORD = "add-prescription";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a prescription to an appointment. \n"
            + "Parameters: "
            + "APPOINTMENT_ID "
            + PREFIX_MEDICINE_NAME + "MEDICINE_NAME "
            + PREFIX_DOSAGE + "DOSAGE "
            + PREFIX_CONSUMPTION_PER_DAY + "CONSUMPTION_PER_DAY \n"
            + "Example: " + COMMAND_WORD + " "
            + "10005 "
            + PREFIX_MEDICINE_NAME + "Paracetamol "
            + PREFIX_DOSAGE + "2 "
            + PREFIX_CONSUMPTION_PER_DAY + "3 ";

    public static final String MESSAGE_SUCCESS = "New Prescription added: %1$s";
    public static final String MESSAGE_DUPLICATE_PRESCRIPTION = "This prescription already exists in the appointment";
    public static final String MESSAGE_APPOINTMENT_DOES_NOT_EXIST = "This appointment does not exist";
    public static final String MESSAGE_PATIENT_ALLERGIC_TO_MEDICINE = "This patient is allergic to %1$s";

    private final int id;
    private final Prescription prescriptionToAdd;

    /**
     * Creates an AddPrescriptionCommand to add the specified {@code Person}
     */
    public AddPrescriptionCommand(int id, Prescription prescription) {
        requireAllNonNull(prescription);
        this.id = id;
        prescriptionToAdd = prescription;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history, GoogleCalendar googleCalendar)
            throws CommandException {
        requireNonNull(model);
        List<Appointment> appointmentList = model.getFilteredAppointmentList();

        // check if appointment exists
        Appointment appointmentToEdit = null;
        for (Appointment appointment : appointmentList) {
            if (appointment.getAppointmentId() == id) {
                appointmentToEdit = appointment;
                break;
            }
        }

        // if appointment does not exist
        if (appointmentToEdit == null) {
            throw new CommandException(String.format(MESSAGE_APPOINTMENT_DOES_NOT_EXIST));
        }

        // check if prescription already exists in appointment
        if (appointmentToEdit.getPrescriptions().contains(prescriptionToAdd)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PRESCRIPTION));
        }

        ArrayList<Prescription> allPrescriptions = new ArrayList<Prescription>();
        allPrescriptions.addAll(appointmentToEdit.getPrescriptions());

        Appointment editedAppointment = new Appointment(new AppointmentId(appointmentToEdit.getAppointmentId()),
                appointmentToEdit.getDoctor(),
                appointmentToEdit.getPatient(),
                appointmentToEdit.getDateTime(),
                appointmentToEdit.getStatus(),
                appointmentToEdit.getComments(),
                allPrescriptions);
        editedAppointment.addPrescription(prescriptionToAdd);
        model.setAppointment(appointmentToEdit, editedAppointment);

        // checking for patient and doctor
        List<Person> personList = model.getFilteredPersonList();
        Doctor doctorToEdit = null;
        Patient patientToEdit = null;
        for (Person person : personList) {
            if (person instanceof Doctor) {
                if (appointmentToEdit.getDoctor().equals(person.getName().toString())) {
                    if ((((Doctor) person).hasAppointment(id))) {
                        doctorToEdit = (Doctor) person;
                    }
                }
            }
            if (person instanceof Patient) {
                if (appointmentToEdit.getPatient().equals(person.getName().toString())) {
                    if (((Patient) person).hasAppointment(id)) {
                        patientToEdit = (Patient) person;
                    }
                }
            }
            if (doctorToEdit != null && patientToEdit != null) {
                break;
            }
        }

        // Doctor only stores upcoming appts while patients store both upcoming and past appt
        if (appointmentToEdit.getStatus().equals("UPCOMING")) {
            if (patientToEdit == null || doctorToEdit == null) {
                throw new CommandException(MESSAGE_APPOINTMENT_DOES_NOT_EXIST);
            }
        } else {
            if (patientToEdit == null) {
                throw new CommandException(MESSAGE_APPOINTMENT_DOES_NOT_EXIST);
            }
        }


        // check if patient is allergic to medicine
        for (Allergy allergy : patientToEdit.getMedicalHistory().getAllergies()) {
            String allergyString = allergy.toString();
            if ((allergyString.toLowerCase().equals(prescriptionToAdd.getMedicineName().toString().toLowerCase()))) {
                throw new CommandException(String.format(MESSAGE_PATIENT_ALLERGIC_TO_MEDICINE, allergy));
            }
        }


        Patient editedPatient = new Patient(patientToEdit.getName(), patientToEdit.getPhone(),
                patientToEdit.getEmail(), patientToEdit.getAddress(), patientToEdit.getRemark(),
                patientToEdit.getTags(), patientToEdit.getTelegramId(), patientToEdit.getUpcomingAppointments(),
                patientToEdit.getPastAppointments(), patientToEdit.getMedicalHistory());
        editedPatient.setAppointment(appointmentToEdit, editedAppointment);
        model.updatePerson(patientToEdit, editedPatient);

        if (doctorToEdit != null) {
            Doctor editedDoctor = new Doctor(doctorToEdit.getName(), doctorToEdit.getPhone(), doctorToEdit.getEmail(),
                    doctorToEdit.getAddress(), doctorToEdit.getRemark(), doctorToEdit.getTags(),
                    doctorToEdit.getUpcomingAppointments());
            editedDoctor.setAppointment(appointmentToEdit, editedAppointment);
            model.updatePerson(doctorToEdit, editedDoctor);
        }



        model.updateFilteredAppointmentList(PREDICATE_SHOW_ALL_APPOINTMENTS);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        model.commitAddressBook();

        EventsCenter.getInstance().post(new PersonPanelSelectionChangedEvent(editedPatient));
        return new CommandResult(String.format(MESSAGE_SUCCESS, prescriptionToAdd.getMedicineName()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof AddPrescriptionCommand)) {
            return false;
        }

        AddPrescriptionCommand e = (AddPrescriptionCommand) o;
        return id == e.id
                && prescriptionToAdd.equals(e.prescriptionToAdd);

    }

}
