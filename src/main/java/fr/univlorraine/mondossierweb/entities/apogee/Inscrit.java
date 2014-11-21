/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

/**
 * représente un étudiant inscrit lors de la rechercher dans la partie enseignant.
 * @author Charlie Dubois
 */
@Entity
@Data
public class Inscrit {
	/**
	 * le code individu.
	 */
	@Id
	@Column(name="COD_IND")
	private String cod_ind;
	/**
	 * le code etudiant.
	 */
	@Column(name="COD_ETU")
	private String cod_etu;
	/**
	 * le nom.
	 */
	@Column(name="NOM")
	private String nom;
	/**
	 * le 1er prenom.
	 */
	@Column(name="LIB_PR1_IND")
	private String prenom;
	/**
	 * la date de naissance.
	 */
	@Column(name="date_nai_ind")
	private String date_nai_ind;
	/**
	 * l'iae.
	 */
	@Column(name="iae")
	private String iae;
	/**
	 * le login.
	 */
	private String login;
	/**
	 * la note de la session de juin.
	 */	
	@Column(name="notej")
	private String notej;
	/**
	 * le résultat de la session de juin.
	 */
	@Column(name="resj")
	private String resj;
	/**
	 * la note de la session de septembre.
	 */
	@Column(name="notes")
	private String notes;
	/**
	 * le résultat de septembre.
	 */
	@Column(name="ress")
	private String ress;
	/**
	 * le code étape où l'étudiant est incrit.
	 */
	private String cod_etp;
	/**
	 * la version de l'étape.
	 */
	private String cod_vrs_vet;
	/**
	 * le libell� de l'étape.
	 */
	private String lib_etp;
	/**
	 * l'e-mail.
	 */
	private String email;
	/**
	 * l'url  de la photo.
	 */
	private String urlphoto;
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "inscrit=  cod_etu : "+cod_etu+" nom : "+nom+" prenom : "+prenom;
	}
	/**
	 * constructeur.
	 */
	/*public Inscrit() {
		super();
		cod_ind = "";
		cod_etu = "";
		nom = "";
		prenom = "";
		date_nai_ind = "";
		iae = "";
		notej = "";
		resj = "";
		notes = "";
		ress = "";
		cod_etp = "";
		cod_vrs_vet = "";
		lib_etp = "";
		email = "";
		urlphoto = "";
	}*/

	
	
}
