package convex.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import convex.core.crypto.AKeyPair;
import convex.core.data.Address;
import convex.core.lang.Symbols;
import convex.java.Convex;
import convex.java.JSON;
import convex.peer.API;
import convex.peer.Server;

public class RemoteClientTest {

	static RESTServer server;
	static int port;
	
	@BeforeAll
	public static void init() {
		Server s=API.launchPeer();
		RESTServer rs=RESTServer.create(s);
		rs.start(0);
		port=rs.getPort();
		server=rs;
	}
	
	@Test 
	public void testCreateAccount() {
		Convex c=Convex.connect("http://localhost:"+port);
		AKeyPair kp=AKeyPair.generate();
		Address addr=c.createAccount(kp);
		assertNotNull(addr);
		
		// second account should be different
		assertNotEquals(addr,c.createAccount(kp));
	}
	
	@Test 
	public void testFaucet() {
		Convex c=Convex.connect("http://localhost:"+port);
		Address addr=c.useNewAccount();
		assertNotNull(addr);
		
		// New account should have zero balance
		assertEquals(0L,c.queryBalance());
		
		long AMT=1000000;
		Map<String,Object> resp=c.faucet(addr, AMT);
		
		// Response should contain amount allocated
		assertEquals(AMT,resp.get("amount"));
		
		// Account should not have requested balance
		assertEquals(AMT,c.queryBalance());
	}
	
	@Test 
	public void testQuery() {
		Convex c=Convex.connect("http://localhost:"+port);
		AKeyPair kp=AKeyPair.generate();
		Address addr=c.createAccount(kp);
		c.setKeyPair(kp);
		c.setAddress(addr);
		
		// Query *address*
		Map<String,Object> res=c.query("*address*");
		assertEquals(addr.longValue(),res.get("value"));
		
		// Query *key*
		res=c.query(Symbols.STAR_KEY.toString());
		assertEquals(JSON.toString(kp.getAccountKey()),res.get("value"));
	}
	
	@Test 
	public void testQueryAccount() {
		Convex c=Convex.connect("http://localhost:"+port);
		AKeyPair kp=AKeyPair.generate();
		Address addr=c.createAccount(kp);
		c.setKeyPair(kp);
		c.setAddress(addr);
		
		Map<String,Object> res=c.queryAccount();
		assertEquals(addr.longValue(),res.get("address"));
		assertEquals(0L,res.get("balance"));
	}
	
	
	
	@AfterAll 
	public static void cleanShutdown() {
		server.stop();
	}
}
