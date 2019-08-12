// 
// Decompiled by Procyon v0.5.36
// 

package junit.awtui;

import junit.framework.TestListener;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import java.net.URL;
import java.awt.image.ImageProducer;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Color;
import java.awt.SystemColor;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import java.awt.MenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.MenuItem;
import java.awt.Menu;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Panel;
import junit.framework.Test;
import java.awt.Font;
import java.awt.Checkbox;
import java.awt.Label;
import java.awt.List;
import java.awt.Button;
import java.awt.TextField;
import java.awt.TextArea;
import junit.framework.TestResult;
import java.util.Vector;
import java.awt.Frame;
import junit.runner.BaseTestRunner;

public class TestRunner extends BaseTestRunner
{
    protected Frame fFrame;
    protected Vector fExceptions;
    protected Vector fFailedTests;
    protected Thread fRunner;
    protected TestResult fTestResult;
    protected TextArea fTraceArea;
    protected TextField fSuiteField;
    protected Button fRun;
    protected ProgressBar fProgressIndicator;
    protected List fFailureList;
    protected Logo fLogo;
    protected Label fNumberOfErrors;
    protected Label fNumberOfFailures;
    protected Label fNumberOfRuns;
    protected Button fQuitButton;
    protected Button fRerunButton;
    protected TextField fStatusLine;
    protected Checkbox fUseLoadingRunner;
    protected static final Font PLAIN_FONT;
    private static final int GAP = 4;
    
    private void about() {
        final AboutDialog about = new AboutDialog(this.fFrame);
        about.setModal(true);
        about.setLocation(300, 300);
        about.setVisible(true);
    }
    
    public void testStarted(final String testName) {
        this.showInfo("Running: " + testName);
    }
    
    public void testEnded(final String testName) {
        this.setLabelValue(this.fNumberOfRuns, this.fTestResult.runCount());
        synchronized (this) {
            this.fProgressIndicator.step(this.fTestResult.wasSuccessful());
        }
    }
    
    public void testFailed(final int status, final Test test, final Throwable t) {
        switch (status) {
            case 1: {
                this.fNumberOfErrors.setText(Integer.toString(this.fTestResult.errorCount()));
                this.appendFailure("Error", test, t);
                break;
            }
            case 2: {
                this.fNumberOfFailures.setText(Integer.toString(this.fTestResult.failureCount()));
                this.appendFailure("Failure", test, t);
                break;
            }
        }
    }
    
    protected void addGrid(final Panel p, final Component co, final int x, final int y, final int w, final int fill, final double wx, final int anchor) {
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
        c.insets = new Insets((y == 0) ? 4 : 0, (x == 0) ? 4 : 0, 4, 4);
        p.add(co, c);
    }
    
    private void appendFailure(String kind, final Test test, final Throwable t) {
        kind = kind + ": " + test;
        final String msg = t.getMessage();
        if (msg != null) {
            kind = kind + ":" + BaseTestRunner.truncate(msg);
        }
        this.fFailureList.add(kind);
        this.fExceptions.addElement(t);
        this.fFailedTests.addElement(test);
        if (this.fFailureList.getItemCount() == 1) {
            this.fFailureList.select(0);
            this.failureSelected();
        }
    }
    
    protected Menu createJUnitMenu() {
        final Menu menu = new Menu("JUnit");
        MenuItem mi = new MenuItem("About...");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                TestRunner.this.about();
            }
        });
        menu.add(mi);
        menu.addSeparator();
        mi = new MenuItem("Exit");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                System.exit(0);
            }
        });
        menu.add(mi);
        return menu;
    }
    
    protected void createMenus(final MenuBar mb) {
        mb.add(this.createJUnitMenu());
    }
    
    protected TestResult createTestResult() {
        return new TestResult();
    }
    
    protected Frame createUI(final String suiteName) {
        final Frame frame = new Frame("JUnit");
        final Image icon = this.loadFrameIcon();
        if (icon != null) {
            frame.setIconImage(icon);
        }
        frame.setLayout(new BorderLayout(0, 0));
        frame.setBackground(SystemColor.control);
        final Frame finalFrame = frame;
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                finalFrame.dispose();
                System.exit(0);
            }
        });
        final MenuBar mb = new MenuBar();
        this.createMenus(mb);
        frame.setMenuBar(mb);
        final Label suiteLabel = new Label("Test class name:");
        (this.fSuiteField = new TextField((suiteName != null) ? suiteName : "")).selectAll();
        this.fSuiteField.requestFocus();
        this.fSuiteField.setFont(TestRunner.PLAIN_FONT);
        this.fSuiteField.setColumns(40);
        this.fSuiteField.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                TestRunner.this.runSuite();
            }
        });
        this.fSuiteField.addTextListener(new TextListener() {
            public void textValueChanged(final TextEvent e) {
                TestRunner.this.fRun.setEnabled(TestRunner.this.fSuiteField.getText().length() > 0);
                TestRunner.this.fStatusLine.setText("");
            }
        });
        (this.fRun = new Button("Run")).setEnabled(false);
        this.fRun.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                TestRunner.this.runSuite();
            }
        });
        final boolean useLoader = this.useReloadingTestSuiteLoader();
        this.fUseLoadingRunner = new Checkbox("Reload classes every run", useLoader);
        if (inVAJava()) {
            this.fUseLoadingRunner.setVisible(false);
        }
        this.fProgressIndicator = new ProgressBar();
        (this.fNumberOfErrors = new Label("0000", 2)).setText("0");
        this.fNumberOfErrors.setFont(TestRunner.PLAIN_FONT);
        (this.fNumberOfFailures = new Label("0000", 2)).setText("0");
        this.fNumberOfFailures.setFont(TestRunner.PLAIN_FONT);
        (this.fNumberOfRuns = new Label("0000", 2)).setText("0");
        this.fNumberOfRuns.setFont(TestRunner.PLAIN_FONT);
        final Panel numbersPanel = this.createCounterPanel();
        final Label failureLabel = new Label("Errors and Failures:");
        (this.fFailureList = new List(5)).addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                TestRunner.this.failureSelected();
            }
        });
        (this.fRerunButton = new Button("Run")).setEnabled(false);
        this.fRerunButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                TestRunner.this.rerun();
            }
        });
        final Panel failedPanel = new Panel(new GridLayout(0, 1, 0, 2));
        failedPanel.add(this.fRerunButton);
        (this.fTraceArea = new TextArea()).setRows(5);
        this.fTraceArea.setColumns(60);
        (this.fStatusLine = new TextField()).setFont(TestRunner.PLAIN_FONT);
        this.fStatusLine.setEditable(false);
        this.fStatusLine.setForeground(Color.red);
        (this.fQuitButton = new Button("Exit")).addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                System.exit(0);
            }
        });
        this.fLogo = new Logo();
        final Panel panel = new Panel(new GridBagLayout());
        this.addGrid(panel, suiteLabel, 0, 0, 2, 2, 1.0, 17);
        this.addGrid(panel, this.fSuiteField, 0, 1, 2, 2, 1.0, 17);
        this.addGrid(panel, this.fRun, 2, 1, 1, 2, 0.0, 10);
        this.addGrid(panel, this.fUseLoadingRunner, 0, 2, 2, 0, 1.0, 17);
        this.addGrid(panel, this.fProgressIndicator, 0, 3, 2, 2, 1.0, 17);
        this.addGrid(panel, this.fLogo, 2, 3, 1, 0, 0.0, 11);
        this.addGrid(panel, numbersPanel, 0, 4, 2, 0, 0.0, 17);
        this.addGrid(panel, failureLabel, 0, 5, 2, 2, 1.0, 17);
        this.addGrid(panel, this.fFailureList, 0, 6, 2, 1, 1.0, 17);
        this.addGrid(panel, failedPanel, 2, 6, 1, 2, 0.0, 10);
        this.addGrid(panel, this.fTraceArea, 0, 7, 2, 1, 1.0, 17);
        this.addGrid(panel, this.fStatusLine, 0, 8, 2, 2, 1.0, 10);
        this.addGrid(panel, this.fQuitButton, 2, 8, 1, 2, 0.0, 10);
        frame.add(panel, "Center");
        frame.pack();
        return frame;
    }
    
    protected Panel createCounterPanel() {
        final Panel numbersPanel = new Panel(new GridBagLayout());
        this.addToCounterPanel(numbersPanel, new Label("Runs:"), 0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0));
        this.addToCounterPanel(numbersPanel, this.fNumberOfRuns, 1, 0, 1, 1, 0.33, 0.0, 10, 2, new Insets(0, 8, 0, 40));
        this.addToCounterPanel(numbersPanel, new Label("Errors:"), 2, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 8, 0, 0));
        this.addToCounterPanel(numbersPanel, this.fNumberOfErrors, 3, 0, 1, 1, 0.33, 0.0, 10, 2, new Insets(0, 8, 0, 40));
        this.addToCounterPanel(numbersPanel, new Label("Failures:"), 4, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 8, 0, 0));
        this.addToCounterPanel(numbersPanel, this.fNumberOfFailures, 5, 0, 1, 1, 0.33, 0.0, 10, 2, new Insets(0, 8, 0, 0));
        return numbersPanel;
    }
    
    private void addToCounterPanel(final Panel counter, final Component comp, final int gridx, final int gridy, final int gridwidth, final int gridheight, final double weightx, final double weighty, final int anchor, final int fill, final Insets insets) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.insets = insets;
        counter.add(comp, constraints);
    }
    
    public void failureSelected() {
        this.fRerunButton.setEnabled(this.isErrorSelected());
        this.showErrorTrace();
    }
    
    private boolean isErrorSelected() {
        return this.fFailureList.getSelectedIndex() != -1;
    }
    
    private Image loadFrameIcon() {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            final URL url = BaseTestRunner.class.getResource("smalllogo.gif");
            return toolkit.createImage((ImageProducer)url.getContent());
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public Thread getRunner() {
        return this.fRunner;
    }
    
    public static void main(final String[] args) {
        new TestRunner().start(args);
    }
    
    public static void run(final Class test) {
        final String[] args = { test.getName() };
        main(args);
    }
    
    public void rerun() {
        final int index = this.fFailureList.getSelectedIndex();
        if (index == -1) {
            return;
        }
        final Test test = this.fFailedTests.elementAt(index);
        this.rerunTest(test);
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
        this.setLabelValue(this.fNumberOfErrors, 0);
        this.setLabelValue(this.fNumberOfFailures, 0);
        this.setLabelValue(this.fNumberOfRuns, 0);
        this.fProgressIndicator.reset();
        this.fRerunButton.setEnabled(false);
        this.fFailureList.removeAll();
        this.fExceptions = new Vector(10);
        this.fFailedTests = new Vector(10);
        this.fTraceArea.setText("");
    }
    
    protected void runFailed(final String message) {
        this.showStatus(message);
        this.fRun.setLabel("Run");
        this.fRunner = null;
    }
    
    public synchronized void runSuite() {
        if (this.fRunner != null && this.fTestResult != null) {
            this.fTestResult.stop();
        }
        else {
            this.setLoading(this.shouldReload());
            this.fRun.setLabel("Stop");
            this.showInfo("Initializing...");
            this.reset();
            this.showInfo("Load Test Case...");
            final Test testSuite = this.getTest(this.fSuiteField.getText());
            if (testSuite != null) {
                (this.fRunner = new Thread() {
                    public void run() {
                        (TestRunner.this.fTestResult = TestRunner.this.createTestResult()).addListener(TestRunner.this);
                        TestRunner.this.fProgressIndicator.start(testSuite.countTestCases());
                        TestRunner.this.showInfo("Running...");
                        final long startTime = System.currentTimeMillis();
                        testSuite.run(TestRunner.this.fTestResult);
                        if (TestRunner.this.fTestResult.shouldStop()) {
                            TestRunner.this.showStatus("Stopped");
                        }
                        else {
                            final long endTime = System.currentTimeMillis();
                            final long runTime = endTime - startTime;
                            TestRunner.this.showInfo("Finished: " + TestRunner.this.elapsedTimeAsString(runTime) + " seconds");
                        }
                        TestRunner.this.fTestResult = null;
                        TestRunner.this.fRun.setLabel("Run");
                        TestRunner.this.fRunner = null;
                        System.gc();
                    }
                }).start();
            }
        }
    }
    
    private boolean shouldReload() {
        return !BaseTestRunner.inVAJava() && this.fUseLoadingRunner.getState();
    }
    
    private void setLabelValue(final Label label, final int value) {
        label.setText(Integer.toString(value));
        label.invalidate();
        label.getParent().validate();
    }
    
    public void setSuiteName(final String suite) {
        this.fSuiteField.setText(suite);
    }
    
    private void showErrorTrace() {
        final int index = this.fFailureList.getSelectedIndex();
        if (index == -1) {
            return;
        }
        final Throwable t = this.fExceptions.elementAt(index);
        this.fTraceArea.setText(BaseTestRunner.getFilteredTrace(t));
    }
    
    private void showInfo(final String message) {
        this.fStatusLine.setFont(TestRunner.PLAIN_FONT);
        this.fStatusLine.setForeground(Color.black);
        this.fStatusLine.setText(message);
    }
    
    protected void clearStatus() {
        this.showStatus("");
    }
    
    private void showStatus(final String status) {
        this.fStatusLine.setFont(TestRunner.PLAIN_FONT);
        this.fStatusLine.setForeground(Color.red);
        this.fStatusLine.setText(status);
    }
    
    public void start(final String[] args) {
        final String suiteName = this.processArguments(args);
        (this.fFrame = this.createUI(suiteName)).setLocation(200, 200);
        this.fFrame.setVisible(true);
        if (suiteName != null) {
            this.setSuiteName(suiteName);
            this.runSuite();
        }
    }
    
    static {
        PLAIN_FONT = new Font("dialog", 0, 12);
    }
}
