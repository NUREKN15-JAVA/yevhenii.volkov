package ua.nure.usermanagement.gui;

import com.mockobjects.dynamic.Mock;
import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.eventdata.StringEventData;
import junit.extensions.jfcunit.finder.NamedComponentFinder;
import ua.nure.usermanagement.User;
import ua.nure.usermanagement.database.DaoFactory;
import ua.nure.usermanagement.database.MockDaoFactory;
import ua.nure.usermanagement.util.TextManager;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

/**
 * A test case for checking proper work and composition of user management applications GUI
 */
public class MainFrameTest extends JFCTestCase {

    public static final int ROW_AMM = 1;
    private Container mainFrame;
    private final int COLUMN_AMM = 3;
    private Mock mockUserDao;
    private List<User> users;

    /**
     * A method, that find a component of specific class inside the visible frame
     *
     * @param componentClass A class of the UI component
     * @param name           A name for the UI component, that need to be found
     * @return Found component. If component is not found, generates assertion error
     */
    private Component find(Class componentClass, String name) {
        NamedComponentFinder finder = new NamedComponentFinder(componentClass, name);
        finder.setWait(0);
        Component component = finder.find(mainFrame, 0);
        assertNotNull(component);
        return component;
    }

    /**
     * Tests UI of BrowsePanel class
     */
    public void testBrowseControl() {
        find(JPanel.class, "browsePanel");
        JTable table = (JTable) find(JTable.class, "userTable");
        assertEquals(COLUMN_AMM, table.getColumnCount());
        assertEquals(TextManager.getString("userTableModel.id"), table.getColumnName(0));
        assertEquals(TextManager.getString("userTableModel.first.name"), table.getColumnName(1));
        assertEquals(TextManager.getString("userTableModel.last.name"), table.getColumnName(2));
        assertEquals(ROW_AMM, table.getRowCount());
        find(JButton.class, "addButton");
        find(JButton.class, "editButton");
        find(JButton.class, "deleteButton");
        find(JButton.class, "detailsButton");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            Properties properties = new Properties();
            properties.setProperty("dao.factory", MockDaoFactory.class.getName());
            DaoFactory.init(properties);
            mockUserDao = ((MockDaoFactory) DaoFactory.getInstance()).getMockUserDao();
            User expectedUser = new User(new Long(0), "Rick", "Sanchez", new Date());
            users = Collections.singletonList(expectedUser);
            mockUserDao.expectAndReturn("findAll", users);
            setHelper(new JFCTestHelper());
            mainFrame = new MainFrame();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainFrame.setVisible(true);
    }

    @Override
    protected void tearDown() throws Exception {
        mainFrame.setVisible(false);
        JFCTestHelper.cleanUp(this);
        mockUserDao.verify();
        super.tearDown();
    }

    /**
     * Tests UI during the process of adding an entry to database. Panels under test: BrowsePanel, AddPanel
     */
    public void testAddUser() {
        String firstName = "Jerry";
        String lastName = "Smith";
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date birthDate = calendar.getTime();

        User user = new User(firstName, lastName, birthDate);
        User expectedUser = new User(new Long(1), firstName, lastName, birthDate);

        mockUserDao.expectAndReturn("create", user, expectedUser);

        List<User> users = new ArrayList<>(this.users);

        users.add(expectedUser);
        mockUserDao.expectAndReturn("findAll", users);

        JTable table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        JButton addButton = (JButton) find(JButton.class, "addButton");
        getHelper().enterClickAndLeave(new MouseEventData(this, addButton));

        find(JPanel.class, "addPanel");
        fillField(firstName, lastName, birthDate);

        find(JButton.class, "cancelButton");
        JButton okButton = ((JButton) find(JButton.class, "okButton"));

        getHelper().enterClickAndLeave(new MouseEventData(this, okButton));

        find(JPanel.class, "browsePanel");

        table = (JTable) find(JTable.class, "userTable");
        assertEquals(2, table.getRowCount());

        mockUserDao.verify();
    }

    /**
     * Tests a case in the process of adding an entry to database, where user aborts insertion.
     */
    public void testAddUserIfCancel() {
        List<User> users = new ArrayList<>(this.users);

        mockUserDao.expectAndReturn("findAll", users);

        JTable table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        JButton addButton = (JButton) find(JButton.class, "addButton");
        getHelper().enterClickAndLeave(new MouseEventData(this, addButton));

        find(JPanel.class, "addPanel");

        JButton cancelButton = (JButton) find(JButton.class, "cancelButton");
        find(JButton.class, "okButton");

        getHelper().enterClickAndLeave(new MouseEventData(this, cancelButton));

        find(JPanel.class, "browsePanel");

        table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        mockUserDao.verify();
    }

    /**
     * A utility method for filling out text fields in classes such as EditPanel or AddPanel
     *
     * @param firstName String to be passed into firstNameField
     * @param lastName  String to be passed into lastNameField
     * @param birthDate Date to be formatted into text and sent into dateOfBirthField
     */
    private void fillField(String firstName, String lastName, Date birthDate) {
        JTextField firstNameField = (JTextField) find(JTextField.class, "firstNameField");
        JTextField lastNameField = (JTextField) find(JTextField.class, "lastNameField");
        JTextField dateOfBirthField = (JTextField) find(JTextField.class, "dateOfBirthField");

        getHelper().sendString(new StringEventData(this, firstNameField, firstName));
        getHelper().sendString(new StringEventData(this, lastNameField, lastName));
        String date = DateFormat.getDateInstance().format(birthDate);
        getHelper().sendString(new StringEventData(this, dateOfBirthField, date));


    }

    /**
     * Tests UI during the process of viewing detailed info of an entry. Panels under test: BrowsePanel, DetailsPanel
     */
    public void testDetails() {
        List<User> users = new ArrayList<>(this.users);

        mockUserDao.expectAndReturn("find", 0L, users.get(0));

        mockUserDao.expectAndReturn("findAll", users);

        JTable table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        JButton detailsButton = (JButton) find(JButton.class, "detailsButton");

        getHelper().enterClickAndLeave(new MouseEventData(this, table));

        getHelper().enterClickAndLeave(new MouseEventData(this, detailsButton));

        find(JPanel.class, "detailsPanel");

        JButton backButton = (JButton) find(JButton.class, "backButton");

        getHelper().enterClickAndLeave(new MouseEventData(this, backButton));

        find(JPanel.class, "browsePanel");

        mockUserDao.verify();
    }

    /**
     * Tests UI during the process of editing an entry in database. Panels under test: BrowsePanel, EditPanel
     */
    public void testEditUser() {
        String firstName = "Rick";
        String lastName = "Smith";
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date birthDate = calendar.getTime();

        User updatedUser = new User(0L, firstName, lastName, birthDate);

        mockUserDao.expectAndReturn("find", 0L, users.get(0));

        mockUserDao.expect("update", updatedUser);

        JTable table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        getHelper().enterClickAndLeave(new MouseEventData(this, table));

        JButton addButton = (JButton) find(JButton.class, "editButton");
        getHelper().enterClickAndLeave(new MouseEventData(this, addButton));

        find(JPanel.class, "editPanel");

        JTextField lastNameField = (JTextField) find(JTextField.class, "lastNameField");

        lastNameField.setText("");

        getHelper().sendString(new StringEventData(this, lastNameField, lastName));

        find(JButton.class, "cancelButton");
        JButton okButton = ((JButton) find(JButton.class, "okButton"));

        List<User> updatedUsers = new ArrayList<>();
        updatedUsers.add(updatedUser);

        mockUserDao.expectAndReturn("findAll", updatedUsers);

        getHelper().enterClickAndLeave(new MouseEventData(this, okButton));

        find(JPanel.class, "browsePanel");

        table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        JButton detailsButton = ((JButton) find(JButton.class, "detailsButton"));

        mockUserDao.expectAndReturn("find", 0L, updatedUser);

        getHelper().enterClickAndLeave(new MouseEventData(this, table));

        getHelper().enterClickAndLeave(new MouseEventData(this, detailsButton));

        lastNameField = (JTextField) find(JTextField.class, "lastNameField");

        assertEquals(lastName, lastNameField.getText());

        mockUserDao.verify();
    }

    /**
     * Tests a case in the process of editing an entry in database, where user aborts changes.
     */
    public void testEditUserIfCancel() {
        List<User> users = new ArrayList<>(this.users);

        mockUserDao.expectAndReturn("findAll", users);

        mockUserDao.expectAndReturn("find", 0L, users.get(0));

        JTable table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        JButton addButton = (JButton) find(JButton.class, "editButton");

        getHelper().enterClickAndLeave(new MouseEventData(this, table));
        getHelper().enterClickAndLeave(new MouseEventData(this, addButton));

        find(JPanel.class, "editPanel");

        JButton cancelButton = (JButton) find(JButton.class, "cancelButton");
        find(JButton.class, "okButton");

        getHelper().enterClickAndLeave(new MouseEventData(this, cancelButton));

        find(JPanel.class, "browsePanel");

        table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        assertEquals("Sanchez", table.getModel().getValueAt(0, 2));

        mockUserDao.verify();
    }

    /**
     * Tests UI during the process of deleting an entry from database. Panels under test: BrowsePanel, DeletePanel
     */
    public void testDeleteUser() {
        JTable table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        ArrayList<User> users = new ArrayList<>(this.users);

        mockUserDao.expectAndReturn("find", 0L, users.get(0));
        mockUserDao.expect("delete", users.get(0));

        JButton deleteButton = (JButton) find(JButton.class, "deleteButton");

        getHelper().enterClickAndLeave(new MouseEventData(this, table));

        getHelper().enterClickAndLeave(new MouseEventData(this, deleteButton));

        find(DeletePanel.class, "deletePanel");

        JButton okButton = (JButton) find(JButton.class, "okButton");

        mockUserDao.expectAndReturn("findAll", users);

        users.remove(this.users.get(0));

        getHelper().enterClickAndLeave(new MouseEventData(this, okButton));

        table = (JTable) find(JTable.class, "userTable");
        assertEquals(0, table.getRowCount());

        mockUserDao.verify();
    }

    /**
     * Tests a case in the process of deleting an entry from database, where user aborts deletion.
     */
    public void testDeleteUserIfCancel() {
        JTable table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        ArrayList<User> users = new ArrayList<>(this.users);

        JButton deleteButton = (JButton) find(JButton.class, "deleteButton");

        getHelper().enterClickAndLeave(new MouseEventData(this, table));

        getHelper().enterClickAndLeave(new MouseEventData(this, deleteButton));

        find(DeletePanel.class, "deletePanel");

        find(JButton.class, "okButton");
        JButton cancelButton = (JButton) find(JButton.class, "cancelButton");

        mockUserDao.expectAndReturn("findAll", users);

        getHelper().enterClickAndLeave(new MouseEventData(this, cancelButton));

        table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        mockUserDao.verify();
    }

    /**
     * Tests a case in the process of deleting an entry from database, where user doesn't select an entry from table.
     * This behaviour is general for all entry-specific panels: EditPanel, DetailsPanel, DeletePanel.
     */
    public void testDeleteUserIfUserNotSelected() {
        JTable table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        ArrayList<User> users = new ArrayList<>(this.users);

        JButton deleteButton = (JButton) find(JButton.class, "deleteButton");

        getHelper().enterClickAndLeave(new MouseEventData(this, deleteButton));

        table = (JTable) find(JTable.class, "userTable");
        assertEquals(1, table.getRowCount());

        mockUserDao.verify();
    }
}
