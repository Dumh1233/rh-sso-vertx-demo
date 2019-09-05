package com.redhat.coolstore.catalog.api;
//package io.vertx.demo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.coolstore.catalog.model.Product;
import com.redhat.coolstore.catalog.verticle.service.CatalogService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.KeycloakHelper;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
// Classes required for OAuth2Handler
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class ApiVerticle extends AbstractVerticle {
	Logger log = LoggerFactory.getLogger(ApiVerticle.class);

	private CatalogService catalogService;

	public ApiVerticle(CatalogService catalogService) {
		this.catalogService = catalogService;
		

	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {

		Router router = Router.router(vertx);
		
		JsonObject keycloakJson = new JsonObject()
				  .put("realm", "base-realm")
				  .put("auth-server-url", "https://sso-rhsso.apps.allinone.pkthakur.in/auth")
				  .put("realm-public-key", "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqfdDPDLSWZmmwXFR62hPqt1teguT6dwgd7YC6hXnO8w55QURbSkoVGTwgGRmLs75X4qeDPjhzqWoBy/VBqH7uhwpgEiqhZmR+l0K091CZ3vljAPTUwi1qFLfCLUpKg+LPaAfA7WrULlvZC1JQpYWWCS+rnY3YT7s/djCvX8i63hGN5GSY+fGmvDi5r6q93q5IuWt7yVenBoWEW2/6SySgPgXzyCt5eld6qz7wYPal8PAdy1/PsYfcjbT3gyNkwW5RAg0MOxKTfVhgAFv0nweycUwofJxo3pIHh9IHJf29ttKPlqkK1y+JKUPfWop65TmTVWLtHfoHXNpDnNwYSZm5wIDAQAB")
				  .put("ssl-required", "none")
				  .put("resource", "vertx")
				  .put("public-client", true)
				  .put("confidential-port", 0);

				OAuth2Auth oauth2 = KeycloakAuth.create(vertx, OAuth2FlowType.AUTH_CODE, keycloakJson);
				OAuth2AuthHandler oauth2Handler = OAuth2AuthHandler.create(oauth2,"http://catalog-service-coolstore-catalog.apps.allinone.pkthakur.in/callback");
				oauth2Handler.setupCallback(router.get("/callback"));
				
	
		router.route("/products/*").handler(oauth2Handler);
		//router.route("/docs/*").handler(oauth2Handler);
		router.get("/products/currentuser").handler(this::currentUser);

		router.get("/products").handler(this::getProducts);
		router.get("/product/:itemId").handler(this::getProduct);
		router.route("/product").handler(BodyHandler.create());
		router.post("/product").handler(this::addProduct);

		// Health Checks
		router.get("/health/readiness").handler(rc -> rc.response().end("OK"));

		HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx).register("health", f -> health(f));
		router.get("/health/liveness").handler(healthCheckHandler);

		// Static content for swagger docs
		router.route().handler(StaticHandler.create());

		

		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("catalog.http.port", 8080),
				result -> {
					if (result.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(result.cause());
					}
				});
	}
	
	
	public void currentUser(RoutingContext context) {
		String accessToken = KeycloakHelper.rawAccessToken(context.user().principal());
		  JsonObject token = KeycloakHelper.parseToken(accessToken);
		  context.response().end("User " + token.getString("preferred_username"));
		}

	private void getProducts(RoutingContext rc) {
		log.info("METHOD_CALLED ");
		catalogService.getProducts(ar -> {
			if (ar.succeeded()) {
				List<Product> products = ar.result();
				log.info("MY PRODUCTS : "+ products);
				JsonArray json = new JsonArray();
				products.stream().map(p -> p.toJson()).forEach(p -> json.add(p));
				rc.response().putHeader("Content-type", "application/json").end(json.encodePrettily());
			} else {
				rc.fail(ar.cause());
			}
		});	
	}

	private void getProduct(RoutingContext rc) {
		String itemId = rc.request().getParam("itemid");
		catalogService.getProduct(itemId, ar -> {
			if (ar.succeeded()) {
				Product product = ar.result();
				JsonObject json;
				if (product != null) {
					json = product.toJson();
					rc.response().putHeader("Content-type", "application/json").end(json.encodePrettily());
				} else {
					rc.fail(404);
				}
			} else {
				rc.fail(ar.cause());
			}
		});
	}

	private void addProduct(RoutingContext rc) {
		JsonObject json = rc.getBodyAsJson();
		catalogService.addProduct(new Product(json), ar -> {
			if (ar.succeeded()) {
				rc.response().setStatusCode(201).end();
			} else {
				rc.fail(ar.cause());
			}
		});
	}

	private void health(Future<Status> future) {
		catalogService.ping(ar -> {
			if (ar.succeeded()) {
				// HealthCheckHandler has a timeout of 1000s. If timeout is exceeded, the future
				// will be failed
				if (!future.isComplete()) {
					future.complete(Status.OK());
				}
			} else {
				if (!future.isComplete()) {
					future.complete(Status.KO());
				}
			}
		});
	}

}
