/**
 * A1icia Tracker
 * 
 * track hall activity
 * 
 * @author hulles
 *
 */
module com.hulles.a1icia.tracker {
	exports com.hulles.a1icia.tracker to guava, com.hulles.a1icia.central;

	requires transitive com.hulles.a1icia;
	requires com.hulles.a1icia.api;
	requires guava;
	requires java.logging;
	requires jedis;
	// to here
	
	provides com.hulles.a1icia.room.UrRoom with com.hulles.a1icia.tracker.TrackerRoom;
}