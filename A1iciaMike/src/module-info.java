/**
 * A1icia Mike
 * 
 * multimedia
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.mike {
	exports com.hulles.a1icia.mike to com.hulles.a1icia.central;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires com.hulles.a1icia.cayenne;
	requires com.hulles.a1icia.media;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires jedis;
    requires javax.json;
    // to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.mike.MikeRoom;
}