package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(AssistanceView.NAME)
public class AssistanceView extends VerticalLayout implements View {
	private static final long serialVersionUID = 7356887304797399383L;

	public static final String NAME = "assistanceView";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		/* Style */
		setMargin(true);
		setSpacing(true);

		/* Titre */
		Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
		title.addStyleName(ValoTheme.LABEL_H1);
		addComponent(title);

		/* Texte */
		addComponent(new Label(applicationContext.getMessage(NAME + ".text", null, getLocale()), ContentMode.HTML));

		/* Accès à la documentation */
		Button docBtn = new Button(applicationContext.getMessage(NAME + ".btnDoc", null, getLocale()), FontAwesome.FILE_TEXT);
		docBtn.addStyleName(ValoTheme.BUTTON_LINK);
		BrowserWindowOpener docBwo = new BrowserWindowOpener(environment.getRequiredProperty("assistance.documentation.url"));
		docBwo.extend(docBtn);
		addComponent(docBtn);

		/* Envoyer un ticket */
		Button helpDeskBtn = new Button(applicationContext.getMessage(NAME + ".btnHelpdesk", null, getLocale()), FontAwesome.AMBULANCE);
		helpDeskBtn.addStyleName(ValoTheme.BUTTON_LINK);
		BrowserWindowOpener helpDeskBwo = new BrowserWindowOpener(environment.getRequiredProperty("assistance.helpdesk.url"));
		helpDeskBwo.extend(helpDeskBtn);
		addComponent(helpDeskBtn);

		/* Envoyer un mail */
		Button contactBtn = new Button(applicationContext.getMessage(NAME + ".btnContact", new Object[] {environment.getRequiredProperty("assistance.contact.mail")}, getLocale()), FontAwesome.ENVELOPE);
		contactBtn.addStyleName(ValoTheme.BUTTON_LINK);
		BrowserWindowOpener contactBwo = new BrowserWindowOpener("mailto: " + environment.getRequiredProperty("assistance.contact.mail"));
		contactBwo.extend(contactBtn);
		addComponent(contactBtn);
	}

	/**
	 * @see com.vaadin.navigator.View${symbol_pound}enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
