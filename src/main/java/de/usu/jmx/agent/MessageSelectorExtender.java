/**
 * 
 */
package de.usu.jmx.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.cxf.transport.jms.JMSConfigFactory;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.apache.cxf.transport.jms.uri.JMSEndpoint;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

/**
 * @author Michel Meier (ExpIT Blog)
 * @version 1.0
 */
public class MessageSelectorExtender implements ClassFileTransformer
{

    public byte[] transform( ClassLoader arg0, String className, Class<?> arg2, ProtectionDomain arg3, byte[] arg4 ) throws IllegalClassFormatException
    {
        /*
         * The enhanced JMSEndpoint class needs to be transformed to bytecode
         * again
         */
        final String jmsEndpointClassname = JMSEndpoint.class.getName();
        final String jmsConfigFactoryClassname = JMSConfigFactory.class.getName();
        final String jmsConfigurationClassname = JMSConfiguration.class.getName();
        final String transformJmsEndpointClassname = jmsEndpointClassname.replace( ".", "/" );
        final String transformJmsConfigFactoryClassname = jmsConfigFactoryClassname.replace( ".", "/" );
        final String declaredMethodName = "createFromEndpoint";

        if ( className.endsWith( transformJmsEndpointClassname ) )
        {
            try
            {
                final ClassPool classPool = ClassPool.getDefault();
                final CtClass clazz = classPool.get( jmsEndpointClassname );
                byte[] byteCode = clazz.toBytecode();
                clazz.detach();
                return byteCode;
            }
            catch ( final Exception ex )
            {
                System.out.println( "Fatal error while adding messageSelector property to class " + jmsEndpointClassname
                + ", please remove agent for valid startup " + ex );
                System.exit( -1 );
            }
        }

        else if ( className.endsWith( transformJmsConfigFactoryClassname ) )
        {
            try
            {
                final ClassPool classPool = ClassPool.getDefault();
                final CtClass jmsConfigFactory = classPool.get( jmsConfigFactoryClassname );
                /*
                 * Because of the unkown Classloader order we need to add the
                 * necessary getter / setter before
                 */
                addMessageSelectorToJMSEndpoint();
                /*
                 * Copy existing method for custom code and rename the old one
                 * for further usage
                 */
                CtMethod existingMethod = jmsConfigFactory.getDeclaredMethod( declaredMethodName );
                String existingMethodName = existingMethod.getName();
                String renamedMethodName = existingMethodName + "$Impl";
                existingMethod.setName( renamedMethodName );
                CtMethod enhancedMethod = CtNewMethod.copy( existingMethod, existingMethodName, jmsConfigFactory, null );

                StringBuffer body = new StringBuffer();
                body.append( "{\n" + jmsConfigurationClassname + " conf = " + renamedMethodName + "($1,$2);\n" );
                body.append( "conf.setMessageSelector($2.getMessageSelector());\n" );
                body.append( "return conf;}" );
                enhancedMethod.setBody( body.toString() );
                jmsConfigFactory.addMethod( enhancedMethod );

                byte[] byteCode = jmsConfigFactory.toBytecode();
                jmsConfigFactory.detach();

                return byteCode;
            }
            catch ( final Exception ex )
            {
                System.out.println( "Fatal error while enhancing " + declaredMethodName + " in class " + jmsConfigFactoryClassname
                + ", please remove agent for valid startup " + ex );
                System.exit( -1 );
            }
        }

        return null;
    }

    private void addMessageSelectorToJMSEndpoint()
    {
        try
        {
            final ClassPool classPool = ClassPool.getDefault();
            final CtClass jmsEndpoint = classPool.get( "org.apache.cxf.transport.jms.uri.JMSEndpoint" );
            CtField messageSelectorField = CtField.make( "private String messageSelector;", jmsEndpoint );
            jmsEndpoint.addField( messageSelectorField );
            CtMethod setterMethod = CtNewMethod.make( "public void setMessageSelector(String selector) {this.messageSelector = selector; }", jmsEndpoint );
            jmsEndpoint.addMethod( setterMethod );
            CtMethod getterMethod = CtNewMethod.make( "public String getMessageSelector() { return this.messageSelector; }", jmsEndpoint );
            jmsEndpoint.addMethod( getterMethod );
        }
        catch ( final Exception ex )
        {
            System.out
            .println( "Fatal error while adding messageSelector property to class org.apache.cxf.transport.jms.uri.JMSEndpoint, please remove agent for valid startup "
            + ex );
            System.exit( -1 );
        }
    }

}
