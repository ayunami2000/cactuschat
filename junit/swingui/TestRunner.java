// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import java.net.URL;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import junit.framework.TestListener;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import java.io.File;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.swing.ImageIcon;
import junit.runner.SimpleTestCollector;
import junit.runner.TestCollector;
import java.awt.Frame;
import java.lang.reflect.Constructor;
import javax.swing.event.DocumentEvent;
import javax.swing.ListModel;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.Container;
import javax.swing.JSplitPane;
import javax.swing.JSeparator;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import javax.swing.JMenuBar;
import javax.swing.Icon;
import junit.runner.Version;
import java.awt.Image;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.BorderLayout;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.LayoutManager;
import java.awt.GridLayout;
import javax.swing.JPanel;
import java.util.Enumeration;
import junit.framework.TestFailure;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;
import junit.runner.FailureDetailView;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import junit.framework.TestResult;
import javax.swing.JFrame;
import junit.runner.BaseTestRunner;

public class TestRunner extends BaseTestRunner implements TestRunContext
{
    private static final int GAP = 4;
    private static final int HISTORY_LENGTH = 5;
    protected JFrame fFrame;
    private Thread fRunner;
    private TestResult fTestResult;
    private JComboBox fSuiteCombo;
    private ProgressBar fProgressIndicator;
    private DefaultListModel fFailures;
    private JLabel fLogo;
    private CounterPanel fCounterPanel;
    private JButton fRun;
    private JButton fQuitButton;
    private JButton fRerunButton;
    private StatusLine fStatusLine;
    private FailureDetailView fFailureView;
    private JTabbedPane fTestViewTab;
    private JCheckBox fUseLoadingRunner;
    private Vector fTestRunViews;
    private static final String TESTCOLLECTOR_KEY = "TestCollectorClass";
    private static final String FAILUREDETAILVIEW_KEY = "FailureViewClass";
    
    public TestRunner() {
        this.fTestRunViews = new Vector();
    }
    
    public static void main(final String[] args) {
        new TestRunner().start(args);
    }
    
    public static void run(final Class test) {
        final String[] args = { test.getName() };
        main(args);
    }
    
    public void testFailed(final int status, final Test test, final Throwable t) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (status) {
                    case 1: {
                        TestRunner.this.fCounterPanel.setErrorValue(TestRunner.this.fTestResult.errorCount());
                        TestRunner.this.appendFailure(test, t);
                        break;
                    }
                    case 2: {
                        TestRunner.this.fCounterPanel.setFailureValue(TestRunner.this.fTestResult.failureCount());
                        TestRunner.this.appendFailure(test, t);
                        break;
                    }
                }
            }
        });
    }
    
    public void testStarted(final String testName) {
        this.postInfo("Running: " + testName);
    }
    
    public void testEnded(final String stringName) {
        this.synchUI();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (TestRunner.this.fTestResult != null) {
                    TestRunner.this.fCounterPanel.setRunValue(TestRunner.this.fTestResult.runCount());
                    TestRunner.this.fProgressIndicator.step(TestRunner.this.fTestResult.runCount(), TestRunner.this.fTestResult.wasSuccessful());
                }
            }
        });
    }
    
    public void setSuite(final String suiteName) {
        this.fSuiteCombo.getEditor().setItem(suiteName);
    }
    
    private void addToHistory(final String suite) {
        for (int i = 0; i < this.fSuiteCombo.getItemCount(); ++i) {
            if (suite.equals(this.fSuiteCombo.getItemAt(i))) {
                this.fSuiteCombo.removeItemAt(i);
                this.fSuiteCombo.insertItemAt(suite, 0);
                this.fSuiteCombo.setSelectedIndex(0);
                return;
            }
        }
        this.fSuiteCombo.insertItemAt(suite, 0);
        this.fSuiteCombo.setSelectedIndex(0);
        this.pruneHistory();
    }
    
    private void pruneHistory() {
        int historyLength = BaseTestRunner.getPreference("maxhistory", 5);
        if (historyLength < 1) {
            historyLength = 1;
        }
        for (int i = this.fSuiteCombo.getItemCount() - 1; i > historyLength - 1; --i) {
            this.fSuiteCombo.removeItemAt(i);
        }
    }
    
    private void appendFailure(final Test test, final Throwable t) {
        this.fFailures.addElement(new TestFailure(test, t));
        if (this.fFailures.size() == 1) {
            this.revealFailure(test);
        }
    }
    
    private void revealFailure(final Test test) {
        final Enumeration e = this.fTestRunViews.elements();
        while (e.hasMoreElements()) {
            final TestRunView v = e.nextElement();
            v.revealFailure(test);
        }
    }
    
    protected void aboutToStart(final Test testSuite) {
        final Enumeration e = this.fTestRunViews.elements();
        while (e.hasMoreElements()) {
            final TestRunView v = e.nextElement();
            v.aboutToStart(testSuite, this.fTestResult);
        }
    }
    
    protected void runFinished(final Test testSuite) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final Enumeration e = TestRunner.this.fTestRunViews.elements();
                while (e.hasMoreElements()) {
                    final TestRunView v = e.nextElement();
                    v.runFinished(testSuite, TestRunner.this.fTestResult);
                }
            }
        });
    }
    
    protected CounterPanel createCounterPanel() {
        return new CounterPanel();
    }
    
    protected JPanel createFailedPanel() {
        final JPanel failedPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        (this.fRerunButton = new JButton("Run")).setEnabled(false);
        this.fRerunButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                TestRunner.this.rerun();
            }
        });
        failedPanel.add(this.fRerunButton);
        return failedPanel;
    }
    
    protected FailureDetailView createFailureDetailView() {
        final String className = BaseTestRunner.getPreference("FailureViewClass");
        if (className != null) {
            Class viewClass = null;
            try {
                viewClass = Class.forName(className);
                return viewClass.newInstance();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this.fFrame, "Could not create Failure DetailView - using default view");
            }
        }
        return new DefaultFailureDetailView();
    }
    
    protected JMenu createJUnitMenu() {
        final JMenu menu = new JMenu("JUnit");
        menu.setMnemonic('J');
        final JMenuItem mi1 = new JMenuItem("About...");
        mi1.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                TestRunner.this.about();
            }
        });
        mi1.setMnemonic('A');
        menu.add(mi1);
        menu.addSeparator();
        final JMenuItem mi2 = new JMenuItem(" Exit ");
        mi2.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                TestRunner.this.terminate();
            }
        });
        mi2.setMnemonic('x');
        menu.add(mi2);
        return menu;
    }
    
    protected JFrame createFrame() {
        final JFrame frame = new JFrame("JUnit");
        final Image icon = this.loadFrameIcon();
        if (icon != null) {
            frame.setIconImage(icon);
        }
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                TestRunner.this.terminate();
            }
        });
        return frame;
    }
    
    protected JLabel createLogo() {
        final Icon icon = getIconResource(BaseTestRunner.class, "logo.gif");
        JLabel label;
        if (icon != null) {
            label = new JLabel(icon);
        }
        else {
            label = new JLabel("JV");
        }
        label.setToolTipText("JUnit Version " + Version.id());
        return label;
    }
    
    protected void createMenus(final JMenuBar mb) {
        mb.add(this.createJUnitMenu());
    }
    
    protected JCheckBox createUseLoaderCheckBox() {
        final boolean useLoader = this.useReloadingTestSuiteLoader();
        final JCheckBox box = new JCheckBox("Reload classes every run", useLoader);
        box.setToolTipText("Use a custom class loader to reload the classes for every run");
        if (inVAJava()) {
            box.setVisible(false);
        }
        return box;
    }
    
    protected JButton createQuitButton() {
        final JButton quit = new JButton(" Exit ");
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                TestRunner.this.terminate();
            }
        });
        return quit;
    }
    
    protected JButton createRunButton() {
        final JButton run = new JButton("Run");
        run.setEnabled(true);
        run.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                TestRunner.this.runSuite();
            }
        });
        return run;
    }
    
    protected Component createBrowseButton() {
        final JButton browse = new JButton("...");
        browse.setToolTipText("Select a Test class");
        browse.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                TestRunner.this.browseTestClasses();
            }
        });
        return browse;
    }
    
    protected StatusLine createStatusLine() {
        return new StatusLine(380);
    }
    
    protected JComboBox createSuiteCombo() {
        final JComboBox combo = new JComboBox();
        combo.setEditable(true);
        combo.setLightWeightPopupEnabled(false);
        combo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyTyped(final KeyEvent e) {
                TestRunner.this.textChanged();
                if (e.getKeyChar() == '\n') {
                    TestRunner.this.runSuite();
                }
            }
        });
        try {
            this.loadHistory(combo);
        }
        catch (IOException ex) {}
        combo.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent event) {
                if (event.getStateChange() == 1) {
                    TestRunner.this.textChanged();
                }
            }
        });
        return combo;
    }
    
    protected JTabbedPane createTestRunViews() {
        final JTabbedPane pane = new JTabbedPane(3);
        final FailureRunView lv = new FailureRunView(this);
        this.fTestRunViews.addElement(lv);
        lv.addTab(pane);
        final TestHierarchyRunView tv = new TestHierarchyRunView(this);
        this.fTestRunViews.addElement(tv);
        tv.addTab(pane);
        pane.addChangeListener(new ChangeListener() {
            public void stateChanged(final ChangeEvent e) {
                TestRunner.this.testViewChanged();
            }
        });
        return pane;
    }
    
    public void testViewChanged() {
        final TestRunView view = this.fTestRunViews.elementAt(this.fTestViewTab.getSelectedIndex());
        view.activate();
    }
    
    protected TestResult createTestResult() {
        return new TestResult();
    }
    
    protected JFrame createUI(final String suiteName) {
        final JFrame frame = this.createFrame();
        final JMenuBar mb = new JMenuBar();
        this.createMenus(mb);
        frame.setJMenuBar(mb);
        final JLabel suiteLabel = new JLabel("Test class name:");
        this.fSuiteCombo = this.createSuiteCombo();
        this.fRun = this.createRunButton();
        frame.getRootPane().setDefaultButton(this.fRun);
        final Component browseButton = this.createBrowseButton();
        this.fUseLoadingRunner = this.createUseLoaderCheckBox();
        this.fStatusLine = this.createStatusLine();
        if (inMac()) {
            this.fProgressIndicator = new MacProgressBar(this.fStatusLine);
        }
        else {
            this.fProgressIndicator = new ProgressBar();
        }
        this.fCounterPanel = this.createCounterPanel();
        this.fFailures = new DefaultListModel();
        this.fTestViewTab = this.createTestRunViews();
        final JPanel failedPanel = this.createFailedPanel();
        this.fFailureView = this.createFailureDetailView();
        final JScrollPane tracePane = new JScrollPane(this.fFailureView.getComponent(), 22, 32);
        this.fQuitButton = this.createQuitButton();
        this.fLogo = this.createLogo();
        final JPanel panel = new JPanel(new GridBagLayout());
        this.addGrid(panel, suiteLabel, 0, 0, 2, 2, 1.0, 17);
        this.addGrid(panel, this.fSuiteCombo, 0, 1, 1, 2, 1.0, 17);
        this.addGrid(panel, browseButton, 1, 1, 1, 0, 0.0, 17);
        this.addGrid(panel, this.fRun, 2, 1, 1, 2, 0.0, 10);
        this.addGrid(panel, this.fUseLoadingRunner, 0, 2, 3, 0, 1.0, 17);
        this.addGrid(panel, this.fProgressIndicator, 0, 3, 2, 2, 1.0, 17);
        this.addGrid(panel, this.fLogo, 2, 3, 1, 0, 0.0, 11);
        this.addGrid(panel, this.fCounterPanel, 0, 4, 2, 0, 0.0, 17);
        this.addGrid(panel, new JSeparator(), 0, 5, 2, 2, 1.0, 17);
        this.addGrid(panel, new JLabel("Results:"), 0, 6, 2, 2, 1.0, 17);
        final JSplitPane splitter = new JSplitPane(0, this.fTestViewTab, tracePane);
        this.addGrid(panel, splitter, 0, 7, 2, 1, 1.0, 17);
        this.addGrid(panel, failedPanel, 2, 7, 1, 2, 0.0, 11);
        this.addGrid(panel, this.fStatusLine, 0, 9, 2, 2, 1.0, 10);
        this.addGrid(panel, this.fQuitButton, 2, 9, 1, 2, 0.0, 10);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocation(200, 200);
        return frame;
    }
    
    private void addGrid(final JPanel p, final Component co, final int x, final int y, final int w, final int fill, final double wx, final int anchor) {
        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.anchor = anchor;
        c.weightx = wx;
        c.fill = fill;
        if (fill == 1 || fill == 3) {
            c.weighty = 1.0;
        }
        c.insets = new Insets((y == 0) ? 10 : 0, (x == 0) ? 10 : 4, 4, 4);
        p.add(co, c);
    }
    
    protected String getSuiteText() {
        if (this.fSuiteCombo == null) {
            return "";
        }
        return (String)this.fSuiteCombo.getEditor().getItem();
    }
    
    public ListModel getFailures() {
        return this.fFailures;
    }
    
    public void insertUpdate(final DocumentEvent event) {
        this.textChanged();
    }
    
    protected Object instanciateClass(final String fullClassName, final Object param) {
        try {
            final Class clazz = Class.forName(fullClassName);
            if (param == null) {
                return clazz.newInstance();
            }
            final Class[] clazzParam = { param.getClass() };
            final Constructor clazzConstructor = clazz.getConstructor((Class[])clazzParam);
            final Object[] objectParam = { param };
            return clazzConstructor.newInstance(objectParam);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void browseTestClasses() {
        final TestCollector collector = this.createTestCollector();
        final TestSelector selector = new TestSelector(this.fFrame, collector);
        if (selector.isEmpty()) {
            JOptionPane.showMessageDialog(this.fFrame, "No Test Cases found.\nCheck that the configured 'TestCollector' is supported on this platform.");
            return;
        }
        selector.show();
        final String className = selector.getSelectedItem();
        if (className != null) {
            this.setSuite(className);
        }
    }
    
    TestCollector createTestCollector() {
        final String className = BaseTestRunner.getPreference("TestCollectorClass");
        if (className != null) {
            Class collectorClass = null;
            try {
                collectorClass = Class.forName(className);
                return collectorClass.newInstance();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this.fFrame, "Could not create TestCollector - using default collector");
            }
        }
        return new SimpleTestCollector();
    }
    
    private Image loadFrameIcon() {
        final ImageIcon icon = (ImageIcon)getIconResource(BaseTestRunner.class, "smalllogo.gif");
        if (icon != null) {
            return icon.getImage();
        }
        return null;
    }
    
    private void loadHistory(final JComboBox combo) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(this.getSettingsFile()));
        int itemCount = 0;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                combo.addItem(line);
                ++itemCount;
            }
            if (itemCount > 0) {
                combo.setSelectedIndex(0);
            }
        }
        finally {
            br.close();
        }
    }
    
    private File getSettingsFile() {
        final String home = System.getProperty("user.home");
        return new File(home, ".junitsession");
    }
    
    private void postInfo(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TestRunner.this.showInfo(message);
            }
        });
    }
    
    private void postStatus(final String status) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TestRunner.this.showStatus(status);
            }
        });
    }
    
    public void removeUpdate(final DocumentEvent event) {
        this.textChanged();
    }
    
    private void rerun() {
        final TestRunView view = this.fTestRunViews.elementAt(this.fTestViewTab.getSelectedIndex());
        final Test rerunTest = view.getSelectedTest();
        if (rerunTest != null) {
            this.rerunTest(rerunTest);
        }
    }
    
    private void rerunTest(final Test test) {
        if (!(test instanceof TestCase)) {
            this.showInfo("Could not reload " + test.toString());
            return;
        }
        Test reloadedTest = null;
        final TestCase rerunTest = (TestCase)test;
        try {
            final Class reloadedTestClass = this.getLoader().reload(test.getClass());
            reloadedTest = TestSuite.createTest(reloadedTestClass, rerunTest.getName());
        }
        catch (Exception e) {
            this.showInfo("Could not reload " + test.toString());
            return;
        }
        final TestResult result = new TestResult();
        reloadedTest.run(result);
        final String message = reloadedTest.toString();
        if (result.wasSuccessful()) {
            this.showInfo(message + " was successful");
        }
        else if (result.errorCount() == 1) {
            this.showStatus(message + " had an error");
        }
        else {
            this.showStatus(message + " had a failure");
        }
    }
    
    protected void reset() {
        this.fCounterPanel.reset();
        this.fProgressIndicator.reset();
        this.fRerunButton.setEnabled(false);
        this.fFailureView.clear();
        this.fFailures.clear();
    }
    
    protected void runFailed(final String message) {
        this.showStatus(message);
        this.fRun.setText("Run");
        this.fRunner = null;
    }
    
    public synchronized void runSuite() {
        if (this.fRunner != null) {
            this.fTestResult.stop();
        }
        else {
            this.setLoading(this.shouldReload());
            this.reset();
            this.showInfo("Load Test Case...");
            final String suiteName = this.getSuiteText();
            final Test testSuite = this.getTest(suiteName);
            if (testSuite != null) {
                this.addToHistory(suiteName);
                this.doRunTest(testSuite);
            }
        }
    }
    
    private boolean shouldReload() {
        return !BaseTestRunner.inVAJava() && this.fUseLoadingRunner.isSelected();
    }
    
    protected synchronized void runTest(final Test testSuite) {
        if (this.fRunner != null) {
            this.fTestResult.stop();
        }
        else {
            this.reset();
            if (testSuite != null) {
                this.doRunTest(testSuite);
            }
        }
    }
    
    private void doRunTest(final Test testSuite) {
        this.setButtonLabel(this.fRun, "Stop");
        this.fRunner = new Thread("TestRunner-Thread") {
            public void run() {
                TestRunner.this.start(testSuite);
                TestRunner.this.postInfo("Running...");
                final long startTime = System.currentTimeMillis();
                testSuite.run(TestRunner.this.fTestResult);
                if (TestRunner.this.fTestResult.shouldStop()) {
                    TestRunner.this.postStatus("Stopped");
                }
                else {
                    final long endTime = System.currentTimeMillis();
                    final long runTime = endTime - startTime;
                    TestRunner.this.postInfo("Finished: " + TestRunner.this.elapsedTimeAsString(runTime) + " seconds");
                }
                TestRunner.this.runFinished(testSuite);
                TestRunner.this.setButtonLabel(TestRunner.this.fRun, "Run");
                TestRunner.this.fRunner = null;
                System.gc();
            }
        };
        (this.fTestResult = this.createTestResult()).addListener(this);
        this.aboutToStart(testSuite);
        this.fRunner.start();
    }
    
    private void saveHistory() throws IOException {
        final BufferedWriter bw = new BufferedWriter(new FileWriter(this.getSettingsFile()));
        try {
            for (int i = 0; i < this.fSuiteCombo.getItemCount(); ++i) {
                final String testsuite = this.fSuiteCombo.getItemAt(i).toString();
                bw.write(testsuite, 0, testsuite.length());
                bw.newLine();
            }
        }
        finally {
            bw.close();
        }
    }
    
    private void setButtonLabel(final JButton button, final String label) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                button.setText(label);
            }
        });
    }
    
    public void handleTestSelected(final Test test) {
        this.fRerunButton.setEnabled(test != null && test instanceof TestCase);
        this.showFailureDetail(test);
    }
    
    private void showFailureDetail(final Test test) {
        if (test != null) {
            final ListModel failures = this.getFailures();
            for (int i = 0; i < failures.getSize(); ++i) {
                final TestFailure failure = failures.getElementAt(i);
                if (failure.failedTest() == test) {
                    this.fFailureView.showFailure(failure);
                    return;
                }
            }
        }
        this.fFailureView.clear();
    }
    
    private void showInfo(final String message) {
        this.fStatusLine.showInfo(message);
    }
    
    private void showStatus(final String status) {
        this.fStatusLine.showError(status);
    }
    
    public void start(final String[] args) {
        final String suiteName = this.processArguments(args);
        (this.fFrame = this.createUI(suiteName)).pack();
        this.fFrame.setVisible(true);
        if (suiteName != null) {
            this.setSuite(suiteName);
            this.runSuite();
        }
    }
    
    private void start(final Test test) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final int total = test.countTestCases();
                TestRunner.this.fProgressIndicator.start(total);
                TestRunner.this.fCounterPanel.setTotal(total);
            }
        });
    }
    
    private void synchUI() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                }
            });
        }
        catch (Exception ex) {}
    }
    
    public void terminate() {
        this.fFrame.dispose();
        try {
            this.saveHistory();
        }
        catch (IOException e) {
            System.out.println("Couldn't save test run history");
        }
        System.exit(0);
    }
    
    public void textChanged() {
        this.fRun.setEnabled(this.getSuiteText().length() > 0);
        this.clearStatus();
    }
    
    protected void clearStatus() {
        this.fStatusLine.clear();
    }
    
    public static Icon getIconResource(final Class clazz, final String name) {
        final URL url = clazz.getResource(name);
        if (url == null) {
            System.err.println("Warning: could not load \"" + name + "\" icon");
            return null;
        }
        return new ImageIcon(url);
    }
    
    private void about() {
        final AboutDialog about = new AboutDialog(this.fFrame);
        about.show();
    }
}
