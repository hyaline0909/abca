import java.util.Iterator;
import java.util.Map;
import java.io.*;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.StringConstant;
import soot.options.Options;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import java.util.*;

public class AndroidInstrument {
	
    static int sourceCount = 0;
    static int notSourceCount = 0;
    static int linenumbertmp;
    static String apkname;

	public static void main(String[] args) {
		
    final ArrayList<Integer> linenumberget = new ArrayList<Integer>();
    final ArrayList<Integer> linenumber = new ArrayList<Integer>();
    final ArrayList<Integer> instrumentlinenumberget = new ArrayList<Integer>();
    final ArrayList<Integer> instrumentlinenumber = new ArrayList<Integer>();

		//prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		//output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);
		// resolve the PrintStream and System soot-classes
		Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
    Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
    PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {


			@Override
			protected void internalTransform(final Body body, String phaseName, @SuppressWarnings("rawtypes") Map options) {
				final PatchingChain<Unit> units = body.getUnits();
				
				//important to use snapshotIterator here
				for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
					final Unit u = iter.next();

          linenumbertmp=u.getJavaSourceStartLineNumber();
          if (linenumbertmp!=-1){
            sourceCount++;
            linenumberget.add(linenumbertmp);
          
					u.apply(new AbstractStmtSwitch() {
						
						public void caseAssignStmt(AssignStmt stmt) {
					
        			Local tmpRef = addTmpRef(body);
        		  Local tmpRef2 = addTmpRef2(body);
        		  Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
          		
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #AssignStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
          		body.validate();
              instrumentlinenumberget.add(linenumbertmp);
						}

            public void caseBreakpointStmt(BreakpointStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #BreakpointStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }

            public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #EnterMonitorStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }

            public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #ExitMonitorStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }
            
            public void caseGotoStmt(GotoStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #GotoStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }
            
            public void caseIfStmt(IfStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #IfStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }

            public void caseInvokeStmt(InvokeStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #InvokeStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }

            public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #LookupSwitchStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }

            public void caseNopStmt(NopStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #NopStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }

            public void caseRetStmt(RetStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #RetStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }

            public void caseReturnStmt(ReturnStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #ReturnStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }

            public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #ReturnVoidStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }

            public void caseTableSwitchStmt(TableSwitchStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #TableSwitchStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }


            public void caseThrowStmt(ThrowStmt stmt) {
          
              Local tmpRef = addTmpRef(body);
              Local tmpRef2 = addTmpRef2(body);
              Local tmpRef3 = addTmpRef3(body);

              Local tmpString = addTmpString(body);
              Local tmpString2 = addTmpString2(body);
              Local tmpString3 = addTmpString3(body);

              // insert "tmpRef = java.lang.System.out;" 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef, Jimple.v().newStaticFieldRef( 
                  Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpRef2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
              //
              SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString, Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString2, Jimple.v().newStaticInvokeExpr( 
                  Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);
              // insert "tmpRef.println(tmpString);" 
              SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
              
              units.insertBefore(Jimple.v().newAssignStmt( 
                tmpString3, StringConstant.v("Linenumber: #" + String.valueOf(linenumbertmp) + " Statement: #ThrowStmt")), stmt);
              // insert "tmpRef.println(tmpString3);"
              SootMethod toCall3 = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");                    
                units.insertBefore(Jimple.v().newInvokeStmt(
                  Jimple.v().newVirtualInvokeExpr(tmpRef, toCall3.makeRef(), tmpString3)), stmt);
              
              //check that we did not mess up the Jimple
              body.validate();
              instrumentlinenumberget.add(linenumbertmp);
            }


            // public void caseIdentityStmt(IdentityStmt stmt) {
            //             // InvokeExpr invokeExpr = stmt.getInvokeExpr();
            //             // if(invokeExpr.getMethod().getName().equals("onDraw")) {
            //               System.out.println("caseIdentityStmt ||| stmt = " + stmt);
            //               Local tmpRef = addTmpRef(body);
            //               Local tmpRef2 = addTmpRef2(body);
            //               Local tmpRef3 = addTmpRef3(body);

            //               Local tmpString = addTmpString(body);
            //               Local tmpString2 = addTmpString2(body);
            //               Local tmpString3 = addTmpString3(body);


              
              
            //     // insert "tmpRef = java.lang.System.out;" 
            //     units.insertBefore(Jimple.v().newAssignStmt( 
            //                   tmpRef, Jimple.v().newStaticFieldRef( 
            //                   Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())), stmt);
            //     units.insertBefore(Jimple.v().newAssignStmt( 
            //                 tmpRef2, Jimple.v().newStaticInvokeExpr( 
            //                 Scene.v().getMethod("<java.lang.Thread: java.lang.Thread currentThread()>").makeRef())), stmt);
            //   //      units.insertBefore(Jimple.v().newAssignStmt( 
            //   //            tmpRef3, Jimple.v().newStaticInvokeExpr( 
            //   //            Scene.v().getMethod("<java.lang.Thread: java.lang.StackTraceElement[] getStackTrace()>").makeRef())), stmt);

                
            //   //       insert "tmpLong = 'HELLO';" 
            //   //      units.insertBefore(Jimple.v().newAssignStmt(tmpString, 
            //   //                    StringConstant.v("HELLO")), stmt);
                        

            //     SootMethod toCall2 = Scene.v().getSootClass("java.lang.Thread").getMethod("java.lang.StackTraceElement[] getStackTrace()"); 

               
            //     units.insertBefore(Jimple.v().newAssignStmt(tmpString, 
            //               Jimple.v().newVirtualInvokeExpr(tmpRef2, toCall2.makeRef()) ), stmt);
                
            //     units.insertBefore(Jimple.v().newAssignStmt(tmpString2, 
            //         Jimple.v().newStaticInvokeExpr( 
            //                   Scene.v().getMethod("<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>").makeRef(),tmpString,IntConstant.v(2))), stmt);

            //     SootMethod toCall3 = Scene.v().getSootClass("java.lang.StackTraceElement").getMethod("java.lang.String getMethodName()");   
            //     // units.insertBefore(Jimple.v().newAssignStmt(tmpString3,
            //     //   Jimple.v().newVirtualInvokeExpr(tmpString2, toCall3.makeRef()) ), stmt);
              
                

            //     // insert "tmpRef.println(tmpString);" 
            //     SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.Object)");                    
            //     units.insertBefore(Jimple.v().newInvokeStmt(
            //                   Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString2)), stmt);
            //                   //check that we did not mess up the Jimple
            //                   body.validate();
            //             // }
            // }

					});
          }
          else{
            notSourceCount++;
          }
				}
			}
            // 
            // 

		}));
		Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
    Scene.v().addBasicClass("java.lang.Thread",SootClass.SIGNATURES);
    Scene.v().addBasicClass("java.lang.StackTraceElement",SootClass.SIGNATURES);	
    Scene.v().addBasicClass("java.lang.reflect.Array",SootClass.SIGNATURES);	

    Scanner scanner = new Scanner(System.in);
    System.out.println("Please type your APK name: ");
    apkname = scanner.nextLine();
    File dir = new File("./"+apkname); 
    if (dir.exists()) { 
    System.out.println("P.S. THIS APK HAD BEEN INSTRUMENTED"); 
    } 
    else { 
    dir.mkdir();
    }

		soot.Main.main(args);

    int i,j;
    Collections.sort(linenumberget);
    j=0;
    linenumber.add(linenumberget.get(0));
    for (i=0;i<linenumberget.size();i++){
      if (Integer.valueOf(linenumberget.get(i)).intValue()!=Integer.valueOf(linenumber.get(j)).intValue()){
        linenumber.add(linenumberget.get(i));
        j++;
      }
    }

    // for (Object o:linenumber) {
    // System.out.println(o);
    // }

    Collections.sort(instrumentlinenumberget);
    j=0;
    instrumentlinenumber.add(instrumentlinenumberget.get(0));
    for (i=0;i<instrumentlinenumberget.size();i++){
      if (Integer.valueOf(instrumentlinenumberget.get(i)).intValue()!=Integer.valueOf(instrumentlinenumber.get(j)).intValue()){
        instrumentlinenumber.add(instrumentlinenumberget.get(i));
        j++;
      }
    }


    System.out.println("-----report-----");
    System.out.println("Transform times: "+String.valueOf(sourceCount+notSourceCount));
    System.out.println("Source codes: "+String.valueOf(sourceCount));
    System.out.println("Not source codes: "+String.valueOf(notSourceCount));
    System.out.println("Instrument times: "+String.valueOf(instrumentlinenumberget.size()));
    System.out.println("Source code's lines: "+String.valueOf(linenumber.size()));
    System.out.println("Final lines: "+String.valueOf(instrumentlinenumber.size()));

    try {
      FileWriter fileWriter = new FileWriter(apkname+"/Denominator.txt");
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

      for (i=0;i<instrumentlinenumber.size();i++){
        bufferedWriter.write(String.valueOf(instrumentlinenumber.get(i)));
        bufferedWriter.newLine();
      }
      bufferedWriter.close();
    }
    catch(IOException ex) {
            System.out.println("Error writing to file '"+ "Denominator.txt" + "'");
    }
    
	}

    private static Local addTmpRef(Body body)
  {
      Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
      body.getLocals().add(tmpRef);
      return tmpRef;
  }
  
  private static Local addTmpRef2(Body body) {
		Local tmpRef2 = Jimple.v().newLocal("tmpRef2",
				RefType.v("java.lang.Thread"));
		body.getLocals().add(tmpRef2);
		return tmpRef2;
	}
  private static Local addTmpRef3(Body body) {
		Local tmpRef3 = Jimple.v().newLocal("tmpRef3",
				RefType.v("java.lang.String"));
		body.getLocals().add(tmpRef3);
		return tmpRef3;
	}
  private static Local addTmpString(Body body)
  {
      Local tmpString = Jimple.v().newLocal("tmpString",ArrayType.v(RefType.v("java.lang.StackTraceElement"), 1) ); 
      body.getLocals().add(tmpString);
      return tmpString;
  }
  private static Local addTmpString2(Body body)
  {
      Local tmpString2 = Jimple.v().newLocal("tmpString2", RefType.v("java.lang.StackTraceElement")); 
      body.getLocals().add(tmpString2);
      return tmpString2;
  }
  private static Local addTmpString3(Body body)
  {
      Local tmpString3 = Jimple.v().newLocal("tmpString3", RefType.v("java.lang.String")); 
      body.getLocals().add(tmpString3);
      return tmpString3;
  }

}
