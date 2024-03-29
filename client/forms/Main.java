package forms;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import javax.swing.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.CardLayout;
import javax.swing.BoxLayout;
import javax.swing.SpringLayout;
import java.awt.GridLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import src.Fila;
import src.Resposta;
import src.Resultado;
import src.ClienteWS;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.awt.event.ActionEvent;
import javax.swing.JTable;

public class Main extends JFrame {

	private JPanel contentPane;
	private JTextField txtRa;
	private JTextField txtCod;
	private JTextField txtNota;
	private JTextField txtFrequencia;
	private static Fila<Resultado> alunos;
	private static Fila<Resultado> alunosSave;
	private static Fila<String> respostas;
	private JTable tblAlunos;
	private static DefaultTableModel model;
	private static Status status = new Status();
	private static Add_status add_status;
	private JList listaRespostas = new JList();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
					alunos = new Fila<>();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel formulario = new JPanel();
		contentPane.add(formulario, BorderLayout.NORTH);
		formulario.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		formulario.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new GridLayout(0, 2, 0, 0));
		
		JLabel lblNewLabel = new JLabel("RA");
		panel_2.add(lblNewLabel);
		
		txtRa = new JTextField();
		panel_2.add(txtRa);
		txtRa.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Código da disciplina");
		panel_2.add(lblNewLabel_1);
		
		txtCod = new JTextField();
		panel_2.add(txtCod);
		txtCod.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Nota");
		panel_2.add(lblNewLabel_2);
		
		txtNota = new JTextField();
		panel_2.add(txtNota);
		txtNota.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Frequência");
		panel_2.add(lblNewLabel_3);
		
		txtFrequencia = new JTextField();
		panel_2.add(txtFrequencia);
		txtFrequencia.setColumns(10);
		
		JButton btnAdicionar = new JButton("Adicionar");
		btnAdicionar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try
				{				
					Resultado aluno = new Resultado(Integer.valueOf(txtRa.getText()), 
													Integer.valueOf(txtCod.getText()), 
													Float.valueOf(txtNota.getText()), 
													Float.valueOf(txtFrequencia.getText()));
					
					alunos.addItem(aluno);
					
					model.addRow(new Object[]{txtRa.getText(), txtCod.getText(), txtNota.getText(), txtFrequencia.getText()});
				}
				catch(NumberFormatException ex)
				{
					status.setStatus("Erro: Resultado em formato inválido. Todos devem ser número.");
					status.setVisible(true);
				}
				catch(Exception ex)
				{
					status.setStatus(ex.getMessage());
					status.setVisible(true);
				}
										
				txtRa.setText("");
				txtCod.setText("");
				txtNota.setText("");
				txtFrequencia.setText("");
			}
		});
		panel_2.add(btnAdicionar);
		
		JButton btnSalvar = new JButton("Salvar");
		btnSalvar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				int rowCount = model.getRowCount();
				for (int r = 0; r < rowCount; r++) {
					model.removeRow(0);
				}
				alunosSave = (Fila<Resultado>) alunos.clone();
				api();
				add_status = new Add_status(alunosSave, respostas);
				add_status.setVisible(true);
				alunos = new Fila<Resultado>();
			}
		});
		panel_2.add(btnSalvar);
		
		JPanel list = new JPanel();
		contentPane.add(list, BorderLayout.CENTER);
		list.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		list.add(panel);
		
		model = new DefaultTableModel();
		tblAlunos = new JTable(model);
		tblAlunos.setFillsViewportHeight(true);
		tblAlunos.setEnabled(false);
		panel.add(tblAlunos);
		
		panel.add(tblAlunos.getTableHeader(), BorderLayout.NORTH);
		panel.add(tblAlunos, BorderLayout.CENTER);
		
		model.addColumn("RA");
		model.addColumn("Disciplina");
		model.addColumn("Nota");
		model.addColumn("Frequência");
	}

	private Resultado primeiroResultado() {
		Resultado ret = null;
		try {
			ret = this.alunos.recuperarItem();
		}
		catch (Exception ex)
		{}
		return ret;
	}

	private void removerResultado() {
		try {
			this.alunos.removerItem();
		}
		catch (Exception ex)
		{}
	}

	private void api() {		
		String[] vetorAlunos = new String[this.alunos.getQtd()];
		respostas = new Fila<String>();
		int indice = 0;

		while (!this.alunos.isVazia()) {
			Resultado enviar = primeiroResultado();
			try {
				Resposta res = ClienteWS.postObjeto(enviar, "http://localhost:3000");
				int code = res.getCode();
				if (code == 200) {
					respostas.addItem("Sucesso");
					vetorAlunos[indice] = "Sucesso";
				} else {
					respostas.addItem("Erro: " + res.getMessage());
					vetorAlunos[indice] = "Erro: " + res.getMessage();
				}
			}
			catch (Exception ex) {
				vetorAlunos[indice] = "Erro: " + ex.getMessage();
			}
			finally {
				removerResultado();
				indice++;
			}
		}
	}
	

}
