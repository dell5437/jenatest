package com.shj.kg.common;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.VCARD;

import java.io.*;
import java.lang.reflect.Field;

public class RDFTest {


    public static void main(String[] args) throws FileNotFoundException {
        queryStatement();
    }

    public static void write(){
        // some definitions
        String personURI = "http://somewhere/JohnSmith";
        String givenName = "John";
        String familyName = "Smith";
        String fullName = givenName + " " + familyName;

        // create an empty Model
        Model model = ModelFactory.createDefaultModel();

        // create the resource
        //   and add the properties cascading style
        Resource johnSmith
                = model.createResource(personURI)
                .addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N,
                        model.createResource()
                                .addProperty(VCARD.Given, givenName)
                                .addProperty(VCARD.Family, familyName));
        // list the statements in the Model
        StmtIterator iter = model.listStatements();

// print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();  // get next statement
            Resource subject = stmt.getSubject();     // get the subject
            Property predicate = stmt.getPredicate();   // get the predicate
            RDFNode object = stmt.getObject();      // get the object

            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }

            System.out.println(" .");
        }
        try {
            OutputStream outputStream=new FileOutputStream(new File("E:\\Project\\jena\\src\\main\\resources\\xml\\test.xml"));
            model.write(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void read() throws FileNotFoundException {
        Model model=ModelFactory.createDefaultModel();
        InputStream inputStream= RDFDataMgr.open("E:\\Project\\jena\\src\\main\\resources\\xml\\test.xml");
        model.read(inputStream,null);
//        model.write(new FileOutputStream(new File("E:\\Project\\jena\\src\\main\\resources\\xml\\read.xml")));
        model.write(System.out);
    }

    public static void prefixes() {
        Model m = ModelFactory.createDefaultModel();
        String nsA = "http://somewhere/else#";
        String nsB = "http://nowhere/else#";
        Resource root = m.createResource( nsA + "root" );
        Property P = m.createProperty( nsA + "P" );
        Property Q = m.createProperty( nsB + "Q" );
        Resource x = m.createResource( nsA + "x" );
        Resource y = m.createResource( nsA + "y" );
        Resource z = m.createResource( nsA + "z" );
        m.add( root, P, x ).add( root, P, y ).add( y, Q, z );
        System.out.println( "# -- no special prefixes defined" );
        m.write( System.out );
        System.out.println( "# -- nsA defined" );
        m.setNsPrefix( "nsA", nsA );
        m.write( System.out );
        System.out.println( "# -- nsA and cat defined" );
        m.setNsPrefix( "cat", nsB );
        m.write( System.out );
    }

    public static void navigating() throws FileNotFoundException {
        String john= "http://somewhere/JohnSmith";
        String inputFile="E:\\Project\\jena\\src\\main\\resources\\xml\\test.xml";
        Model model=ModelFactory.createDefaultModel();
        InputStream inputStream= FileManager.get().open(inputFile);

        if (inputStream == null) {
            throw new IllegalArgumentException( "File: " + inputFile + " not found");
        }
        // 读取RDF/XML文件
        model.read(new InputStreamReader(inputStream),"");

        // retrieve the Adam Smith vcard resource from the model
        // 从资源中取出john的名片
        Resource vcard=model.getResource(john);

        // 取出属性 N 的值
        Resource name=(Resource) vcard.getProperty(VCARD.N).getObject();

        // 取出属性 FN 的值
        String fullName=vcard.getProperty(VCARD.FN).getString();

//        System.out.println("name : "+name+"\n"+"full name : "+fullName);

        // 添加两个属性 nickname
        vcard.addProperty(VCARD.NICKNAME,"james");
        vcard.addProperty(VCARD.NICKNAME,"wade");

        // 获取nickname 列表
        StmtIterator iterator=vcard.listProperties(VCARD.NICKNAME);
        while (iterator.hasNext()){
            System.out.println("nick name: "+iterator.nextStatement().toString());
        }

        OutputStream outputStream=new FileOutputStream(new File("E:\\Project\\jena\\src\\main\\resources\\xml\\out.xml"));
        model.write(outputStream);

    }
    public static Model getModel(){
        //        String john= "http://somewhere/JohnSmith";
        String inputFile="E:\\Project\\jena\\src\\main\\resources\\xml\\test.xml";
        Model model=ModelFactory.createDefaultModel();
        InputStream inputStream= FileManager.get().open(inputFile);

        if (inputStream == null) {
            throw new IllegalArgumentException( "File: " + inputFile + " not found");
        }
        // 读取RDF/XML文件
        model.read(new InputStreamReader(inputStream),"");
        return model;
    }


    public static void query(){

        Model model=getModel();
        // 查询所有含有 VCARD.FN 属性的文档
        ResIterator iterator=model.listSubjectsWithProperty(VCARD.FN);
        if(iterator.hasNext()){
            System.out.println(" resource  FN  :\n");
            while (iterator.hasNext()){
                System.out.println(iterator.nextResource().getProperty(VCARD.FN).getString());
            }
        }
    }

    public static void queryStatement(){
        Model model=getModel();

        StmtIterator iterator=model.listStatements(new SimpleSelector(null,VCARD.FN,(Resource)null)
        {
            @Override
            public boolean selects(Statement s) {
                return s.getString().contains("oh");
            }
        });
        if (iterator.hasNext()) {
            System.out.println("The database contains vcards for:");
            while (iterator.hasNext()) {
                System.out.println("  " + iterator.nextStatement()
                        .getString());
            }
        } else {
            System.out.println("No Smith's were found in the database");
        }

    }
}
