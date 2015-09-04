package fr.univlorraine.mondossierweb.views.windows;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.vaadin.ObjetBaseCollectionGroupe;

/**
 * Fenêtre du détail des groupes
 */
@Configurable(preConstruction=true)
public class DetailGroupesWindow extends Window {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "groupesWindow";


	public static final String ID_PROPERTY = "id";
	public static final String CODE_COLLECTION_PROPERTY = "cod_coll";
	public static final String CODE_GROUPE_PROPERTY = "cod_gpe";
	public static final String LIBELLE_GROUPE_PROPERTY = "lib_gpe";
	public static final String CAP_MAX_PROPERTY = "cap_max";
	public static final String CAP_INT_PROPERTY = "cap_int";
	public static final String NB_INSCRITS_PROPERTY = "nb_inscrits";

	/* les champs de la table */
	public static final String[] DETAIL_FIELDS_ORDER = {CODE_COLLECTION_PROPERTY, CODE_GROUPE_PROPERTY,LIBELLE_GROUPE_PROPERTY,
		CAP_MAX_PROPERTY,CAP_INT_PROPERTY,NB_INSCRITS_PROPERTY};


	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;


	private List<ElpDeCollection> lgroupes;

	private String elpLibelle;

	private String annee;

	private String vetLibelle;

	/**
	 * Crée une fenêtre
	 */
	public DetailGroupesWindow(List<ElpDeCollection> lg, String elpLib, String vetLib,String anneeUniv) {
		super();
		lgroupes = lg;
		elpLibelle = elpLib;
		int anneenplusun = Integer.parseInt(anneeUniv) + 1;
		annee = anneeUniv+"/"+anneenplusun;
		vetLibelle = vetLib;
		init();
	}

	private void init() {

		/* Style */
		setWidth(80, Unit.PERCENTAGE);
		setHeight(95, Unit.PERCENTAGE);
		setModal(true);
		setResizable(false);


		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setMargin(true);
		layout.setSpacing(true);

		/* Titre */
		setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));


		//Sous titre avec l'année 
		HorizontalLayout titleLayout = new HorizontalLayout();
		titleLayout.setSizeFull();
		titleLayout.setHeight("20px");
		Label labelAnneeUniv = new Label();
		labelAnneeUniv.setValue(applicationContext.getMessage(NAME+".label.anneeuniv", null, getLocale())+" <b>"+ annee+"</b>");
		labelAnneeUniv.setContentMode(ContentMode.HTML);
		titleLayout.addComponent(labelAnneeUniv);
		titleLayout.setComponentAlignment(labelAnneeUniv, Alignment.MIDDLE_LEFT);

		if(StringUtils.hasText(vetLibelle)){
			Label labelEtape = new Label();
			labelEtape.setValue(applicationContext.getMessage(NAME+".label.vet", null, getLocale())+" : <b>"+ vetLibelle+"</b>");
			labelEtape.setContentMode(ContentMode.HTML);
			titleLayout.addComponent(labelEtape);
			titleLayout.setComponentAlignment(labelEtape, Alignment.MIDDLE_RIGHT);
		}
		
		layout.addComponent(titleLayout);



		Panel panelDetailGroupes= new Panel(elpLibelle);
		panelDetailGroupes.setSizeFull();

		if(lgroupes!=null && lgroupes.size()>0){
			TreeTable detailGroupesTable = new TreeTable();
			detailGroupesTable.setSizeFull();
			HierarchicalContainer hc = new HierarchicalContainer();
			hc.addContainerProperty(ID_PROPERTY, String.class, "");
			hc.addContainerProperty(CODE_COLLECTION_PROPERTY, String.class, "");
			hc.addContainerProperty(CODE_GROUPE_PROPERTY, String.class, "");
			hc.addContainerProperty(LIBELLE_GROUPE_PROPERTY, String.class, "");
			hc.addContainerProperty(CAP_MAX_PROPERTY, String.class, "");
			hc.addContainerProperty(CAP_INT_PROPERTY, String.class, "");
			hc.addContainerProperty(NB_INSCRITS_PROPERTY, String.class, "");
			detailGroupesTable.setContainerDataSource(hc);
			int id = 0;
			for(ElpDeCollection edc : lgroupes){
				id++;
				for(CollectionDeGroupes cdg : edc.getListeCollection()){
					id++;
					ObjetBaseCollectionGroupe obj = new ObjetBaseCollectionGroupe();
					obj.setId(""+id);
					obj.setCod_coll(cdg.getCodCollection());
					obj.setCap_max("");
					obj.setCap_int("");
					obj.setNb_inscrits("");
					Item itemCollection = hc.addItem(obj.getId());
					renseignerItem(itemCollection,obj);

					for(Groupe gpe : cdg.getListeGroupes()){
						id++;
						ObjetBaseCollectionGroupe objgpe = new ObjetBaseCollectionGroupe();
						objgpe.setId(""+id);
						objgpe.setCod_gpe(gpe.getCodGroupe());
						objgpe.setLib_gpe(gpe.getLibGroupe());
						objgpe.setCap_max(""+gpe.getCapMaxGpe());
						objgpe.setCap_int(""+gpe.getCapIntGpe());
						objgpe.setNb_inscrits(""+gpe.getNbInscrits());
						Item i = hc.addItem(objgpe.getId());
						renseignerItem(i,objgpe);
						detailGroupesTable.setParent(objgpe.getId(), obj.getId());
						detailGroupesTable.setChildrenAllowed(objgpe.getId(), false);
					}

					detailGroupesTable.setCollapsed(obj.getId(), false);

				}
			}


			detailGroupesTable.addContainerProperty(ID_PROPERTY, String.class, "");
			detailGroupesTable.addContainerProperty(CODE_COLLECTION_PROPERTY, String.class, "");
			detailGroupesTable.addContainerProperty(CODE_GROUPE_PROPERTY, String.class, "");
			detailGroupesTable.addContainerProperty(LIBELLE_GROUPE_PROPERTY, String.class, "");
			detailGroupesTable.addContainerProperty(CAP_MAX_PROPERTY, String.class, "");
			detailGroupesTable.addContainerProperty(CAP_INT_PROPERTY, String.class, "");
			detailGroupesTable.addContainerProperty(NB_INSCRITS_PROPERTY, String.class, "");

			detailGroupesTable.setVisibleColumns(DETAIL_FIELDS_ORDER);

			detailGroupesTable.setColumnHeader(CODE_COLLECTION_PROPERTY, applicationContext.getMessage(NAME+".table.codecollection", null, getLocale()));
			detailGroupesTable.setColumnHeader(CODE_GROUPE_PROPERTY, applicationContext.getMessage(NAME+".table.codegroupe", null, getLocale()));
			detailGroupesTable.setColumnHeader(LIBELLE_GROUPE_PROPERTY, applicationContext.getMessage(NAME+".table.libgroupe", null, getLocale()));
			detailGroupesTable.setColumnHeader(CAP_MAX_PROPERTY, applicationContext.getMessage(NAME+".table.capmax", null, getLocale()));
			detailGroupesTable.setColumnHeader(CAP_INT_PROPERTY, applicationContext.getMessage(NAME+".table.capint", null, getLocale()));
			detailGroupesTable.setColumnHeader(NB_INSCRITS_PROPERTY, applicationContext.getMessage(NAME+".table.nbinscrits", null, getLocale()));
			
			detailGroupesTable.setColumnCollapsingAllowed(true);
			detailGroupesTable.setColumnReorderingAllowed(false);
			detailGroupesTable.setSelectable(false);
			detailGroupesTable.setImmediate(true);
			detailGroupesTable.addStyleName("scrollabletable");

			panelDetailGroupes.setContent(detailGroupesTable);

		}


		layout.addComponent(panelDetailGroupes);



		Panel panelCollectionInfo= new Panel(applicationContext.getMessage(NAME+".info.title", null, getLocale()));
		panelCollectionInfo.setIcon(FontAwesome.INFO_CIRCLE);

		panelCollectionInfo.addStyleName("significationpanel");


		VerticalLayout significationLayout = new VerticalLayout();
		significationLayout.setMargin(true);
		significationLayout.setSpacing(true);

		Label mapSignificationLabel=new Label(applicationContext.getMessage(NAME+".collection.info", null, getLocale()));
		mapSignificationLabel.setStyleName(ValoTheme.LABEL_SMALL);
		mapSignificationLabel.setContentMode(ContentMode.HTML);


		significationLayout.addComponent(mapSignificationLabel);

		panelCollectionInfo.setContent(significationLayout);

		layout.addComponent(panelCollectionInfo);


		layout.setExpandRatio(panelDetailGroupes, 1);


		setContent(layout);


		/* Centre la fenêtre */
		center();


	}

	private void renseignerItem(Item i, ObjetBaseCollectionGroupe obj) {

		i.getItemProperty(ID_PROPERTY).setValue(obj.getId());
		i.getItemProperty(CODE_COLLECTION_PROPERTY).setValue(obj.getCod_coll());
		i.getItemProperty(CODE_GROUPE_PROPERTY).setValue(obj.getCod_gpe());
		i.getItemProperty(LIBELLE_GROUPE_PROPERTY).setValue(obj.getLib_gpe());
		i.getItemProperty(CAP_MAX_PROPERTY).setValue(""+obj.getCap_max());
		i.getItemProperty(CAP_INT_PROPERTY).setValue(""+obj.getCap_int());
		i.getItemProperty(NB_INSCRITS_PROPERTY).setValue(""+obj.getNb_inscrits());

	}


	/** Formats the position in a column containing Date objects. */
	class LibelleElpColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<ElementPedagogique> bid = (BeanItem<ElementPedagogique>) item;
			ElementPedagogique el = (ElementPedagogique) bid.getBean();
			Label libLabel = new Label();

			if(StringUtils.hasText(el.getLibelle())){

				//indentation des libelles dans la liste:
				int rg = new Integer(el.getLevel());
				String libelp = el.getLibelle();
				String lib = "";
				for (int j = 2; j <= rg; j++) {
					lib= lib + "&#160;&#160;&#160;&#160;&#160;";
				}
				if(el.getLevel()==1 && !el.isEpreuve()){
					libelp="<b>"+libelp+"</b>";
				}
				if(el.isEpreuve()){
					libelp="<i>"+libelp+"</i>";
				}
				libLabel.setValue(lib+libelp);
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
	}







}
