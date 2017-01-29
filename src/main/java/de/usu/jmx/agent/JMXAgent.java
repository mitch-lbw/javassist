/**
 * 
 */
package de.usu.jmx.agent;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

/**
 * <strong>Projekt: jmx.agent</strong><br />
 * <strong>Klasse: de.usu.jmx.agent.JMXAgent</strong><br />
 *
 * <hr noshade />
 * <strong>Beschreibung:</strong><br />
 * Beispiel-Klasse fuer die USU Code Conventions
 * <br />
 * <hr noshade />
 * <strong>Historie:</strong><br />
 * 27.01.2017;usimmeier;Erstellung<br />
 *
 * <br />
 * <hr noshade />
 * 
 * @since 1.4
 * @author usimmeier
 * @version 1.0
 */
public class JMXAgent
{
    public static void premain( String agentArgs ) throws Throwable
    {
        System.out.println( "----- premain -------" );
//        Thread th = new Thread( new Runnable()
//        {
//
//            public void run()
//            {
//                
//            }
//        } );
//        th.start();
//        
//        return;
    }
    
    public static void premain( String agentArgs, Instrumentation ins ) throws Throwable
    {      
        System.out.println( "----- premain instrumentaiton -------" );
        final MessageSelectorExtender extender = new MessageSelectorExtender();
        ins.addTransformer( extender );
    }

    private static void getMBeanServer()
    {
        checkServerAvailable();
        System.out.println( "---------- Getting MBean Server" );
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        System.out.println( server.getDefaultDomain() );
    }

    private static void checkServerAvailable()
    {
        boolean connected = false;

        while ( !connected )
        {

            try
            {

                connected = true;

            }
            catch ( Exception e )
            {
                System.out.println( "Server not available" );
            }
            try
            {
                Thread.sleep( 1000l );
            }
            catch ( InterruptedException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
