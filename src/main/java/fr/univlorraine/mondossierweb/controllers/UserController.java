package fr.univlorraine.mondossierweb.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.directory.Attributes;

import org.aopalliance.intercept.MethodInvocation;
import org.esupportail.portal.ws.client.PortalGroup;
import org.esupportail.portal.ws.client.PortalUser;
import org.esupportail.portal.ws.client.support.uportal.CachingUportalServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.util.MethodInvocationUtils;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.entities.apogee.Utilisateur;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteService;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteServiceImpl;
import fr.univlorraine.mondossierweb.services.apogee.UtilisateurService;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;

/**
 * Gestion de l'utilisateur
 */
@Component
public class UserController {

	private Logger LOG = LoggerFactory.getLogger(UserController.class);

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserDetailsService userDetailsService;
	@Resource
	private transient LdapUserSearch ldapUserSearch;
	@Resource
	private transient MethodSecurityInterceptor methodSecurityInterceptor;
	@Resource
	private transient CachingUportalServiceImpl portalService;
	/** {@link UtilisateurServiceImpl} */
	@Resource
	private UtilisateurService utilisateurService;




	/**
	 * type utilisateur étudiant.
	 */
	public static final String STUDENT_USER = "student";
	/**
	 * type correspondant à un utilisateur dont le login doit être exclu de l'application.
	 */
	public static final String LOGIN_EXCLU = "exclu";

	/**
	 * type utilisateur enseignant.
	 */
	public static final String TEACHER_USER = "teacher";

	/**
	 * type utilisateur non-autorisé.
	 */
	public static final String UNAUTHORIZED_USER = "unauthorized";




	/**
	 * @return l'authentification courante
	 */
	public Authentication getCurrentAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * @param viewClass
	 * @return true si l'utilisateur peut accéder à la vue
	 */
	public boolean canCurrentUserAccessView(Class<? extends View> viewClass) {
		MethodInvocation methodInvocation = MethodInvocationUtils.createFromClass(viewClass, "enter");
		Collection<ConfigAttribute> configAttributes = methodSecurityInterceptor.obtainSecurityMetadataSource().getAttributes(methodInvocation);
		/* Renvoie true si la vue n'est pas sécurisée */
		if (configAttributes.isEmpty()) {
			return true;
		}
		/* Vérifie que l'utilisateur a les droits requis */
		try {
			methodSecurityInterceptor.getAccessDecisionManager().decide(getCurrentAuthentication(), methodInvocation, configAttributes);
		} catch (InsufficientAuthenticationException | AccessDeniedException e) {
			return false;
		}
		return true;
	}

	/**
	 * @return user utilisateur courant
	 */
	public UserDetails getCurrentUser() {
		return (UserDetails) getCurrentAuthentication().getPrincipal();
	}

	/**
	 * @return username de l'utilisateur courant
	 */
	public String getCurrentUserName() {
		return getCurrentAuthentication().getName();
	}

	/**
	 * @return true si l'utilisateur a pris le rôle d'un autre utilisateur
	 */
	public boolean isUserSwitched() {
		for (GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			if (SwitchUserFilter.ROLE_PREVIOUS_ADMINISTRATOR.equals(ga.getAuthority())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Change le rôle de l'utilisateur courant
	 * @param username
	 */
	public void switchToUser(String username) {
		if (!StringUtils.hasText(username)) {
			throw new IllegalArgumentException("username ne peut être vide.");
		}

		/* Vérifie que l'utilisateur existe */
		try {
			userDetailsService.loadUserByUsername(username);
		} catch (UsernameNotFoundException unfe) {
			Notification.show(applicationContext.getMessage("admin.switchUser.usernameNotFound", new Object[] {username}, UI.getCurrent().getLocale()), Notification.Type.WARNING_MESSAGE);
			return;
		}

		String switchToUserUrl = environment.getRequiredProperty("switchUser.switchUrl") + "?" + SwitchUserFilter.SPRING_SECURITY_SWITCH_USERNAME_KEY + "=" + username;
		Page.getCurrent().open(switchToUserUrl, null);
	}

	/**
	 * Rétabli le rôle original de l'utilisateur
	 */
	public void switchBackToPreviousUser() {
		Page.getCurrent().open(environment.getRequiredProperty("switchUser.exitUrl"), null);
	}

	public boolean isEnseignant() {
		if(MainUI.getCurrent().getTypeUser()==null){
			determineTypeUser();
		}
		if(MainUI.getCurrent().getTypeUser()!=null && MainUI.getCurrent().getTypeUser().equals(TEACHER_USER)){
			return true;
		}
		return false;
	}

	public boolean isEtudiant() {
		if(MainUI.getCurrent().getTypeUser()==null){
			determineTypeUser();
		}
		if(MainUI.getCurrent().getTypeUser()!=null && MainUI.getCurrent().getTypeUser().equals(STUDENT_USER)){
			return true;
		}
		return false;
	}




	public void determineTypeUser() {

		MainUI.getCurrent().setTypeUser(null);
		List<String> type = typeLdap(getCurrentUserName());

		if (StringUtils.hasText(PropertyUtils.getTypeEtudiantLdap()) && type!=null &&
				type.contains(PropertyUtils.getTypeEtudiantLdap())) { 
			MainUI.getCurrent().setTypeUser(STUDENT_USER);
		} else {

			//on cherche a savoir si l'employé a acces (ex: c'est un enseignant)
			//si il est autorisé type=enseignant, sinon type=non-autorise



			boolean useruportal = false;
			try {
				//on reucupère la liste de groupes mis dans le bean security
				List<String> listegroupes = PropertyUtils.getListeGroupesUportalAutorises();

				//on test si on est en portlet
				if (listegroupes != null && listegroupes.size()>0) {

					//recupère l'utilisateur uportal
					PortalUser portaluser = portalService.getUser(getCurrentUserName());

					//on cherche si il appartient a un groupe
					useruportal = false;



					//on regarde si il appartient a un des groupes
					for (String nomgroupe : listegroupes) {
						//si on est pas déjà sur qu'il appartient a un groupe:
						if(!useruportal) {
							//on cherche le groupe
							PortalGroup pgroup = portalService.getGroupByName(nomgroupe);
							if (pgroup != null) {
								//on regarde si l'utilisateur appartient a ce groupe
								if (portalService.isUserMemberOfGroup(portaluser, pgroup)) {
									//c'est un utilisateur uportal
									useruportal = true;
								}
							} 
						}
					}
				}
			} catch (Exception e) {
				//Test présence dans la table utilisateur de Apogee
				LOG.info("PROBLEME DE CONNEXION AUX GROUPES UPORTAL");
			}

			if (useruportal) {
				//c'est un utilisateur uportal il est donc autorisé en tant qu'enseignant
				LOG.info("USER "+getCurrentUserName()+" ENSEIGNANT VIA UPORTAL");
				MainUI.getCurrent().setTypeUser(TEACHER_USER);


			} else {
				//va voir dans apogée
				System.out.println("USER "+getCurrentUserName()+" NON ENSEIGNANT VIA UPORTAL -> Recherche Apogée");


				//On test si on doit chercher l'utilisateur dans Apogee
				if(PropertyUtils.isLoginApogee()){
					//Test de la présence dans la table utilisateur d'Apogee
					//on regarde si il est dans la table utilisateur 
					try {
						Utilisateur uti = utilisateurService.findUtilisateur(getCurrentUserName().toUpperCase());

						if (uti != null) {
							MainUI.getCurrent().setTypeUser(TEACHER_USER);
						} else {
							MainUI.getCurrent().setTypeUser(UNAUTHORIZED_USER);
							LOG.info("utilisateur "+getCurrentUserName()+" n' est pas dans le LDAP en tant qu' etudiant, n'appartient à aucun groupe uportal, et n'est pas dans la table utilisateur d'APOGEE -> UTILISATEUR NON AUTORISE !");

						}
					} catch (Exception ex) {
						LOG.error("Probleme lors de la vérification de l'existence de l'utilisateur dans la table Utilisateur de Apogee",ex);
					}
				}else{
					MainUI.getCurrent().setTypeUser(UNAUTHORIZED_USER);
					LOG.info("Utilisateur "+getCurrentUserName()+" n' est pas dans le LDAP en tant qu' etudiant, n'appartient à aucun groupe uportal -> UTILISATEUR NON AUTORISE !");
				}


			}
		}

	}

	/**
	 * 
	 * @param login de l'utilisateur
	 * @return le type retourné par ldap.
	 */
	public List<String> typeLdap(final String login) {
		try {
			if(ldapUserSearch.searchForUser(getCurrentUserName())!=null){
				String[] vals= ldapUserSearch.searchForUser(getCurrentUserName()).getStringAttributes(PropertyUtils.getAttributLdapEtudiant());
				if(vals!=null){
					List<String> listeValeurs = Arrays.asList(vals);
					return listeValeurs;
				}
			}
			return null;
		} catch (Exception e) {
			LOG.error("Probleme à la recuperation de l'utilisateur : "+login+" dans le LDAP",e);
			return null;
		}
	}

}
