package seedu.address.logic.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPatientsAndDoctors.getTypicalAddressBookWithPatientAndDoctor;

import org.junit.Test;

import seedu.address.logic.CommandHistory;
import seedu.address.model.HealthBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.testutil.GoogleCalendarStub;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests and unit tests for RemarkCommand.
 */
public class RemarkCommandTest {

    private static final GoogleCalendarStub GOOGLE_CALENDAR_STUB = new GoogleCalendarStub();

    private Model model = new ModelManager(getTypicalAddressBookWithPatientAndDoctor(), new UserPrefs());
    private CommandHistory commandHistory = new CommandHistory();

    @Test
    public void execute_addRemarkUnfilteredList_success() {
        Person firstPerson = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(firstPerson).withRemark(VALID_REMARK_BOB).build();
        RemarkCommand remarkCommand = new RemarkCommand(firstPerson.getName(), firstPerson.getPhone(),
                new Remark(editedPerson.getRemark().value));

        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, editedPerson.getName());

        Model expectedModel = new ModelManager(new HealthBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updatePerson(firstPerson, editedPerson);
        expectedModel.commitAddressBook();

        assertCommandSuccess(remarkCommand, model, commandHistory, expectedMessage, expectedModel);
    }

    @Test
    public void execute_deleteRemarkUnfilteredList_success() {
        Person firstPerson = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(firstPerson).withRemark("").build();
        RemarkCommand remarkCommand = new RemarkCommand(firstPerson.getName(), firstPerson.getPhone(),
                new Remark(editedPerson.getRemark().value));

        String expectedMessage = String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS, editedPerson.getName());

        Model expectedModel = new ModelManager(new HealthBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updatePerson(firstPerson, editedPerson);
        expectedModel.commitAddressBook();

        assertCommandSuccess(remarkCommand, model, commandHistory, expectedMessage, expectedModel);

    }

    @Test
    public void execute_patientDoesNotExist_failure() {
        Person toEdit = new PersonBuilder().withName(VALID_NAME_AMY).build();
        RemarkCommand remarkCommand = new RemarkCommand(toEdit.getName(), null, toEdit.getRemark());

        assertCommandFailure(remarkCommand, model, commandHistory, remarkCommand.MESSAGE_INVALID_PERSON_FAILURE);
    }

    @Test
    public void execute_duplicateNames_failure() {
        Person personToEdit = model.getFilteredPersonList().get(1);

        RemarkCommand remarkCommand = new RemarkCommand(personToEdit.getName(), null, personToEdit.getRemark());
        assertCommandFailure(remarkCommand, model, commandHistory, remarkCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void executeUndoRedo_validIndexUnfilteredList_success() throws Exception {

        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withRemark(VALID_REMARK_BOB).build();
        RemarkCommand remarkCommand = new RemarkCommand(personToEdit.getName(), null,
                new Remark(VALID_REMARK_BOB));
        Model expectedModel = new ModelManager(new HealthBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updatePerson(personToEdit, editedPerson);
        expectedModel.commitAddressBook();

        // remark -> first person remark edited
        remarkCommand.execute(model, commandHistory, GOOGLE_CALENDAR_STUB);

        // undo -> reverts addressbook back to previous state and filtered person list to show all persons
        expectedModel.undoAddressBook();
        assertCommandSuccess(new UndoCommand(), model, commandHistory, UndoCommand.MESSAGE_SUCCESS, expectedModel);

        // redo -> same first person edited again
        expectedModel.redoAddressBook();
        assertCommandSuccess(new RedoCommand(), model, commandHistory, RedoCommand.MESSAGE_SUCCESS, expectedModel);

    }

    @Test
    public void equals() {
        final RemarkCommand standardCommand = new RemarkCommand(new Name(VALID_NAME_AMY), new Phone(VALID_PHONE_AMY),
                new Remark(VALID_REMARK_AMY));

        // same values -> returns true
        RemarkCommand commandWithSameValues = new RemarkCommand(new Name(VALID_NAME_AMY), new Phone(VALID_PHONE_AMY),
                new Remark(VALID_REMARK_AMY));
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different name -> returns false
        assertFalse(standardCommand.equals(new RemarkCommand(new Name(VALID_NAME_BOB), new Phone(VALID_PHONE_AMY),
                new Remark(VALID_REMARK_AMY))));
        // different phone -> returns false
        assertFalse(standardCommand.equals(new RemarkCommand(new Name(VALID_NAME_AMY), new Phone(VALID_PHONE_BOB),
                new Remark(VALID_REMARK_AMY))));
        // different remark -> returns false
        assertFalse(standardCommand.equals(new RemarkCommand(new Name(VALID_NAME_AMY), new Phone(VALID_PHONE_AMY),
                new Remark(VALID_REMARK_BOB))));
    }



}
