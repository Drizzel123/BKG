package com.group.proseminar.knowledge_graph.reader;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Startup graphical user interface where the user can enter the ontology of the articles s/he wants to fetch and build the knowledge graph from.
 * 
 * @author Sibar Soumi
 *
 */

class StartView extends JFrame {

	private JTextField ontology_chooser;
	private JButton submit;

	private Semaphore start_view_wait = null;

	StartView(Semaphore start_view_wait, Controller controller) {
		this.start_view_wait = start_view_wait;
		setTitle("Knowledge Graph Generator");
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JPanel up = new JPanel();
		add(up);

		up.add(new JLabel("Enter the odb which you want to use: "));
		ontology_chooser = new JTextField(20);
		up.add(ontology_chooser);
		submit = new JButton("Start");
		add(submit);

		submit.addActionListener((e) -> {
			controller.setOdb(ontology_chooser.getText());
			this.dispose();
			start_view_wait.release();
		});

		ontology_chooser.setText("Scientist");

		pack();
		setLocation(
				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()
						/ 2 - (this.getWidth() / 2),
				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()
						/ 2 - (this.getHeight() / 2));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

}
