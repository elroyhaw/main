package seedu.address.model.patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import seedu.address.model.appointment.Appointment;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.model.tag.Tag;

/**
 * Represents a Patient in the health book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Patient extends Person {
    // Variables
    private String telegramId;
    private MedicalHistory medicalHistory;
    private List<Appointment> upcomingAppointments;
    private List<Appointment> pastAppointments;

    // Constructor

    public Patient(Name name, Phone phone, Email email, Address address, Remark remark,
                   Set<Tag> tags, String telegramId) {
        super(name, phone, email, address, remark, tags);
        setTelegramId(telegramId);
        upcomingAppointments = new ArrayList<>();
        pastAppointments = new ArrayList<>();
        this.medicalHistory = new MedicalHistory();
    }

    public Patient(Name name, Phone phone, Email email, Address address, Remark remark,
                   Set<Tag> tags, String telegramId, List<Appointment> upcomingAppointments,
                   List<Appointment> pastAppointments) {
        super(name, phone, email, address, remark, tags);
        setTelegramId(telegramId);
        this.upcomingAppointments = upcomingAppointments;
        this.pastAppointments = pastAppointments;
        this.medicalHistory = new MedicalHistory();
    }

    public Patient(Name name, Phone phone, Email email, Address address, Remark remark,
                   Set<Tag> tags, String telegramId, List<Appointment> upcomingAppointments,
                   List<Appointment> pastAppointments, MedicalHistory medicalHistory) {
        super(name, phone, email, address, remark, tags);
        setTelegramId(telegramId);
        this.upcomingAppointments = upcomingAppointments;
        this.pastAppointments = pastAppointments;
        this.medicalHistory = medicalHistory;

    }

    public List<Appointment> getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public List<Appointment> getPastAppointments() {
        return pastAppointments;
    }

    public String getTelegramId() {
        return telegramId;
    }

    public MedicalHistory getMedicalHistory() {
        return medicalHistory;
    }

    public void setTelegramId(String telegramId) {
        this.telegramId = telegramId;
    }

    public void setMedicalHistory(MedicalHistory medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    /**
     * Adds allergy into the medical history of patient
     */
    public void addAllergy(String allergy) {
        medicalHistory.addAllergy(allergy);
    }

    /**
     * Adds condition into the medical history of patient
     */
    public void addCondition(String condition) {
        medicalHistory.addCondition(condition);
    }

    /**
     * Adds an upcoming appointment to the patient's queue of upcoming appointment.
     */
    public void addUpcomingAppointment(Appointment appointment) {
        upcomingAppointments.add(appointment);
    }

    /**
     * Deletes appointment from patient's queue of upcoming appointment.
     */
    public void deleteAppointment(Appointment appointment) {
        upcomingAppointments.remove(appointment);
    }

    /**
     * Completes the {@code appointment} of the patient, placing the records of the appointment in to the stack of
     * appointments
     */
    public void completeUpcomingAppointment(Appointment targetAppointment) {
        Appointment appointmentToRemove = null;
        for (Appointment app : upcomingAppointments) {
            if (app.isSameAppointment(targetAppointment)) {
                appointmentToRemove = app;
            }
        }
        appointmentToRemove.completeAppointment();
        upcomingAppointments.remove(appointmentToRemove);
        pastAppointments.add(appointmentToRemove);
    }

    /**
     * Check if the patient contains a certain appointment by {@code appointmentId}
     */
    public boolean hasAppointment(int appointmentId) {
        for (Appointment app : upcomingAppointments) {
            if (app.getAppointmentId() == appointmentId) {
                return true;
            }
        }
        return false;
    }
}
