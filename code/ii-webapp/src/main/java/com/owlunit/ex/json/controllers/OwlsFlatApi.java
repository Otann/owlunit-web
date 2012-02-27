package com.owlunit.ex.json.controllers;

/**
 * @author Ilya Pimenov
 *         Owls Proproetary
 */
public interface OwlsFlatApi extends OwlsMovieApi, OwlsUserApi, OwlsRecommenderApi, OwlsPosterApi {

    String version();

    // mva-ha-ha !
}
