import os 
import sys
import codecs

total_covered_rate=0
classes={}
def process_file(matrix,argv):
    global total_covered_rate
    global classes
    # 所有的class method linenumber 資訊
    for root,dirnames,filenames in os.walk(argv[1]):
        for filename in filenames:
            if not filename.startswith(".") and not filename.startswith("R") and not filename.startswith("BuildConfig") :
                infile=codecs.open(os.path.join(root,filename),'r')
                lines=infile.readlines()
                infile.close()

                class_name=""
                method_name=""
                In_Line=False
                hasThread_In_Line=False
                hasPrintIn_In_LIne=False
                for line in lines:
                    if line.find(".interface ")!=-1:
                        break
                    elif line.find(".class ")!=-1:
                        class_name=line.replace(".class ","").strip()
                        class_name=class_name.split().pop().replace("/",".")
                        # print(class_name)
                        if classes.get(class_name)==None: 
                            classes[class_name]={}
                    elif line.find(".method ")!=-1:
                        method_name=line.replace(".method ","").strip()
                        method_name=method_name.split(" ")[-1].split("(")[0]
                            # .method public add(Ljava/lang/Class;Landroid/os/Bundle;JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V

                        # print(method_name)
        
                        if classes[class_name].get(method_name)==None: 
                            classes[class_name][method_name]=set()
                    
                    

                    elif line.find(".line ")!=-1: 
                        In_Line=True                      
                        linenumber=int(line.replace(".line ","").strip())
                        hasThread_In_Line=False
                        hasPrintIn_In_LIne=False

                    elif line.find(".end method")!=-1 or line.find("return")!=-1:
                        In_Line=False
                             
                    if In_Line:
                       if line.find("currentThread()Ljava/lang/Thread;")!=-1:
                         hasThread_In_Line=True
                       if line.find("println(Ljava/lang/Object;)")!=-1:
                         hasPrintIn_In_LIne=True
                   
                    if In_Line and (hasThread_In_Line and  hasPrintIn_In_LIne):
                         classes[class_name][method_name].add(linenumber)

                    



                    

        
    for key,value in classes.items():
        for key2,value2 in value.items():
            value2=list(value2)

    
    for item in matrix:
        # print("Testcase ",item[0])
        # print("  Step ",item[1])
        covered_lines=[]

        total_linenumber=0
        total_covered_linenumber=0
        total_method=0
        total_covered_method=0

        for key,value in classes.items():
            key_class=key
            # print(key_class)

            # print("     Class: "+key)
            for key2,value2 in value.items():
                if len(value2) !=0:
                    # print("             Method: "+key2)
                    key_method=key2

                    if not key_class in item[1].keys():
                        item[1][key_class]={}
                    if not key_method in item[1][key_class].keys(): 
                        item[1][key_class][key_method]=[]
                    if len(set(item[1][key_class][key_method])-set(value2))>0:
                        item[1][key_class][key_method]=list(set(item[1][key_class][key_method])&set(value2))


                    # print("             Method name: "+key_method)
                    # print("                  Lines: "+str(item[1][key_class][key_method])+"/"+str(sorted(value2)))

                    # print("                  Line coverage percentage: "+str(100*(len(item[1][key_class][key_method])/len(value2)))+"%")
                    
                    total_covered_linenumber=total_covered_linenumber+len(item[1][key_class][key_method])
                    total_linenumber=total_linenumber+len(value2)
                    if len(item[1][key_class][key_method])/len(value2) > 0:
                        total_covered_method+=1
                    total_method+=1
                    # 超過100% print xxxx代表錯誤
                    if 100*(len(item[1][key_class][key_method])/len(value2)) > 100:
                        print("item[1][key_class][key_method]),(value2)"+str(item[1][key_class][key_method])+","+str((value2)))
                        print("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        print("total_linenumber: "+str(total_linenumber))
        print("total_covered_linenumber: "+str(total_covered_linenumber))
        # print("total_covered_rate: "+str((total_covered_linenumber/total_linenumber)*100)+"%")
        total_covered_rate=(total_covered_linenumber/total_linenumber)*100
        print("total_method: "+str(total_method))
        print("total_covered_method: "+str(total_covered_method))





Button_covered_classes={}
def main(argv,Current_Step_number,button_id):
    global total_covered_rate
    global Button_covered_classes

    
    matrix=[]
    # if button_id==(0,0):
    #     return 0,{},0
    
   
    for root,dirnames,filenames in os.walk(argv[2]):
        if root.split('/')[-1].startswith("trace"):
            Testcase_number=int(root.split('/')[-1].replace("trace","")) 
            classes_logcat={}
            class_name_logcat=""
            method_name_logcat=""
            Step_number=-1


            if len(filenames)>1:
                # print("filenames type: "+str(type(filenames)))
                # print("filenames[1:] : "+str(filenames[1:]))
                filenames2=list(filenames[1:])
                tmplist=sorted(filenames2,key=lambda x: int(x.split('_')[0].replace("step","")))
                # print("tmplist: "+str(tmplist))
                filenames3=[]
                filenames3.append(filenames[0])
                filenames3.extend(tmplist)
                # print("filenames[1:].sort :"+str(filenames3))
                # filenames2=list(filenames[0]).extend(filenames[1:].sort(key=lambda x: [int(y) for y in x.split('_')[0].replace("step","")]))
                # print(str(filenames.sort(key=lambda x: [int(y) for y in x.split('_')[0].replace("step","").replace("Initial","-1")])))
            else:
                filenames3=list(filenames)

            for filename in filenames3:
                # print("filename: "+str(filename))             
                
                if filename.split('_')[0].replace("step","")!="Initial":
                    if type(int(filename.split('_')[0].replace("step",""))) is int:
                        Step_number=int(filename.split('_')[0].replace("step",""))
                        # filenames.sort(key=lambda x: [int(y) for y in x.split('_')[0].replace("step","")]) 
                # print("filename: "+str(filename.split('_')[0].replace("step","")))
                # print("Step_number: "+str(Step_number))
                # print("Current_Step_number: "+str(Current_Step_number))
                # print("os.path.join(root,filename):"+str(os.path.join(root,filename)))
                infile_logcat=open(os.path.join(root,filename),'r')
                lines_logcat=infile_logcat.readlines()
                infile_logcat.close()
                # print(filename)
                for line in lines_logcat:
                    
                    if Step_number==Current_Step_number and Step_number!=-1:
                        if not button_id in Button_covered_classes:
                            Button_covered_classes[button_id]={}

                    if line.count('I/System.out')!=0:
                        class_name_logcat=line.split("(")[1].split(": ")[1].split(".")
                        class_name_logcat=".".join(class_name_logcat[0:len(class_name_logcat)-1])
                        
                        # class初始化
                        if not class_name_logcat in classes_logcat:
                            classes_logcat[class_name_logcat]={}
                        
                        # for each button coverage

                        if Step_number==Current_Step_number and Step_number!=-1:
                            if not class_name_logcat in Button_covered_classes[button_id]:
                               Button_covered_classes[button_id][class_name_logcat]={}
                                   # print("Initial class"+str(Button_covered_classes[class_name_logcat]))

                        # method初始化 
                        if not line.split("(")[1].split(": ")[1].split(".")[-1] in classes_logcat[class_name_logcat]:
                            classes_logcat[class_name_logcat][line.split("(")[1].split(": ")[1].split(".")[-1]]=set()

                        # for each button coverage
                        if Step_number==Current_Step_number and Step_number!=-1:
                            if not line.split("(")[1].split(": ")[1].split(".")[-1] in Button_covered_classes[button_id][class_name_logcat]:
                                Button_covered_classes[button_id][class_name_logcat][line.split("(")[1].split(": ")[1].split(".")[-1]]=set()

                       # 如果logcat有顯示line的話加入linenumber 
                        if  line.split("(")[-1].split(":")[-1].replace(")","").replace("\n","").replace(" ","").isdigit():                         
                            classes_logcat[class_name_logcat][line.split("(")[1].split(": ")[1].split(".")[-1]].add(int(line.split("(")[-1].split(":")[-1].replace(")","").replace("\n","").replace(" ","")))
                             
                            # 加到該button
                            if Step_number==Current_Step_number and Step_number!=-1:
                                Button_covered_classes[button_id][class_name_logcat][line.split("(")[1].split(": ")[1].split(".")[-1]].add(int(line.split("(")[-1].split(":")[-1].replace(")","").replace("\n","").replace(" ","")))
                              


                        # print("---------------------------------------")
            for key,value in classes_logcat.items():
                for key2,value2 in value.items():
                    classes_logcat[key][key2]=sorted(list(value2))



            matrix.append((Testcase_number,classes_logcat,Button_covered_classes))
    process_file(matrix,argv)
    if button_id==(0,0):
        return total_covered_rate,{},0

    # print("Step_number: "+str(Step_number)+"and Current_Step_number: "+str(Current_Step_number))
    if Step_number==Current_Step_number and Step_number!=-1:
        # print(Button_covered_classes)
        button_coverage_rate=0
        if filename.split('_')[0].replace("step","")!="Initial" and Button_covered_classes[button_id]!={}:
             button_coverage_rate=caculate_button_coverage_rate(Button_covered_classes[button_id])
        print("caculate_button_coverage_rate:  "+str(button_coverage_rate*100)+"%")

        return total_covered_rate,Button_covered_classes,button_coverage_rate
    # return total_covered_rate,Button_covered_classes,caculate_button_coverage_rate

   

def caculate_button_coverage_rate(Button_covered_classes):
    global classes
    button_covered_line=[]
    button_covered_method_all_line=[]
    button_covered_line_statistics=[]
    button_covered_method_all_line_statistics=[]
    result=0

    for button_class,button_methods in Button_covered_classes.items():
        # print(button_class)
        for button_method in button_methods.keys():
            # print(button_method)
            button_covered_line=[]
            button_covered_method_all_line=[]
            if button_class in classes.keys() and button_method in classes[button_class].keys():

                button_covered_line.extend(list(Button_covered_classes[button_class][button_method]))
                button_covered_method_all_line.extend(list(classes[button_class][button_method]))
                if len(set(button_covered_line)-set(button_covered_method_all_line))>0:
                        button_covered_line=list(set(button_covered_line)&set(button_covered_method_all_line))

                # print(" class: "+str(button_class)+" method: "+str(button_method)+" button_covered_line"+str(sorted(button_covered_line)))
                # print(" class: "+str(button_class)+" method: "+str(button_method)+" button_all_line"+str(sorted(button_covered_method_all_line)))
                button_covered_line_statistics.extend(list(Button_covered_classes[button_class][button_method]))
                button_covered_method_all_line_statistics.extend(list(classes[button_class][button_method]))
                


    # print(button_covered_line)
    # print(button_covered_method_all_line)
    if len(button_covered_method_all_line_statistics)!=0:
        # print("(button_covered_line_statistics)/(button_covered_method_all_line_statistics)"+str(button_covered_line_statistics)+"/"+str(button_covered_method_all_line_statistics)) 
        result=len(button_covered_line_statistics)/len(button_covered_method_all_line_statistics)
    # print("button_covered_line"+str(button_covered_line_statistics))

    return result







if __name__ == "__main__":
    main(sys.argv)