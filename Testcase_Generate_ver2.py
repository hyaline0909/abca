import os
import sys
import shutil
import random
import time
import xml.etree.ElementTree as ET
import Testcase_Coverage_ver2 as Coverage
import codecs
import collections

# from Node import Node
# from Edge import Edge



NodeList=[]
EdgeList=[]

target=0
button_tapped=None

All_traveled_path=[]
screen_button_tap={}




FrameLayoutList=[]
def getViews_FrameLayout(root):
    global FrameLayoutList
    returnList=[]
    for child in root:
        if "android.widget.FrameLayout" in child.attrib["class"]:
            FrameLayoutList.append(child)
            # print(child.attrib["bounds"]+",") 
            # for item in FrameLayoutList:
            #     print(str(item.attrib["bounds"]))
        returnList.append(child)
        returnList.extend(getViews_FrameLayout(child))
    return returnList



def getViews(root):
    returnList=[]
    for child in root:
        returnList.append(child)
        returnList.extend(getViews(child))
    return returnList






def ripping(root,screen_number_inputargument):

    # global screen_number
    # print("screen_number_inputargument: "+str(screen_number_inputargument))
    screen_number=screen_number_inputargument
    global target
    global button_tapped
    global screen_button_tap
    global NodeList
    global step


    # screen_button,screen_button_tapped



    print("")
    viewList=getViews(root)
    # print(viewList)
    coordinateList=[]
    button_name_List=[]
    i=0
    print("screen_number: "+str(screen_number))
    print("Button Coordinates: ")

    for view in viewList:
        if view.attrib["clickable"]=="true":
            # print(view)            
            
            leftBound=int(view.attrib["bounds"].replace("[","").split("]")[0].split(",")[0])
            topBound=int(view.attrib["bounds"].replace("[","").split("]")[0].split(",")[1])
            rightBound=int(view.attrib["bounds"].replace("[","").split("]")[1].split(",")[0])
            bottomBound=int(view.attrib["bounds"].replace("[","").split("]")[1].split(",")[1])
            if view.attrib["text"]=="":
                print(" {0:3d} . [{1:3d},{2:3d}] [{3:3d},{4:4d}]  =>  (".format(i,leftBound,topBound,rightBound,bottomBound)+str((leftBound+rightBound)/2)+","+str((topBound+bottomBound)/2)+")")
            else:
                print(" {0:3d} . [{1:3d},{2:3d}] [{3:3d},{4:4d}]  =>  (".format(i,leftBound,topBound,rightBound,bottomBound)+str((leftBound+rightBound)/2)+","+str((topBound+bottomBound)/2)+")    -->"+view.attrib["text"])    
            i+=1
            button_name_List.append(str(view.attrib["text"]))
            coordinateList.append(((leftBound+rightBound)/2,(topBound+bottomBound)/2))
            screen_button_tap[screen_number][1].add(((leftBound+rightBound)/2,(topBound+bottomBound)/2))
    
    target=0
    # target=len(coordinateList)-1
    # target=random.randint(0,len(coordinateList)-1)

    # use DFS algo to tap all buttons
    # if tapped,then back
  
    
  
    while coordinateList[target] in screen_button_tap[screen_number][0]:
        if target<len(coordinateList)-1:
            target+=1
                         
        # random.randint(0,len(coordinateList)-1)

        # 如果都點過回上一個父畫面
        else:
            print("this screen has traveled all views and should backward ....")
            restart()
            screen_button_tap[screen_number][2]="traveled screen"

            for inEdge in NodeList[screen_number].inEdges:
                if inEdge[0]<screen_number:

                    print("travel back to parent screen"+str(inEdge[0])+".....................................")
                    for travel in NodeList[inEdge[0]].toScreenInputs:
                    # os.system("adb shell input tap "+str(coordinateList[target][0])+" "+str(coordinateList[target][1]))                     
                        time.sleep(1)
                        os.system("adb shell input tap "+str(travel[1]))
                        print("adb shell input tap "+str(travel[1]))
                        time.sleep(1)             
                screen_number=inEdge[0]
                try:
                      time.sleep(1)
                      os.system("adb shell /system/bin/uiautomator dump /data/Screen_xml_info.xml")
                      os.system("adb pull /data/Screen_xml_info.xml ")
                      tree = ET.parse('Screen_xml_info.xml')
                      root = tree.getroot()
                except:
                      input("After screen loaded press Enter to continue...")
                      os.system("adb shell /system/bin/uiautomator dump /data/Screen_xml_info.xml")
                      os.system("adb pull /data/Screen_xml_info.xml ")
                      tree = ET.parse('Screen_xml_info.xml')
                      root = tree.getroot()
                return (screen_number,root)
    
    print("coordinateList:  "+str(coordinateList))
    print("coordinateList[target]:  "+str(target)+"."+str(button_name_List[target])+" "+str(coordinateList[target]))
    print("screen_button_tap[screen_number][0]:  "+str(screen_button_tap[screen_number][0]))
    # print("target = "+str(target))   

    os.system("adb shell input tap "+str(coordinateList[target][0])+" "+str(coordinateList[target][1]))

    # print("adb shell input tap"+str(coordinateList[target][0])+" "+str(coordinateList[target][1]))
    button_tapped=str(coordinateList[target][0])+" "+str(coordinateList[target][1])
    screen_button_tap[screen_number][0].add((coordinateList[target][0],coordinateList[target][1]))

    return (screen_number,root)






# 1.回到之前相同畫面(跟現在相同沒關係!!!!)然後現在的畫面還有沒點完的按鈕  2.離開這隻程式
def check_problem(screen_button_tap,current_screen_number,next_screen_number,root,All_traveled_path):
    global packageName
    global FrameLayoutList
    global folder
    # 離開這這隻程式
    getViews_FrameLayout(root)
    viewList1 = list(FrameLayoutList)
    
    FrameLayoutList=[]
    App_is_over=False
    if screen_button_tap[current_screen_number][2]=="traveled screen":
        print("Travel over.................")
        final_model_structure(NodeList,EdgeList,screen_number,screen_button_tap)
        for i in range(5):
            print("Travel over.................")
            input("After configure the setting press Enter to continue...")
            tapByCoverageRate(NodeList,screen_button_tap,folder) 
        exit()



    for item in viewList1:
        if item.attrib["package"]!=packageName:
            App_is_over=True
  
    # 如果還沒點完就離開要回到相同畫面或是離開這隻程式




    if  current_screen_number!=next_screen_number:
        if (next_screen_number in screen_button_tap.keys() and  screen_button_tap[current_screen_number][2]!="traveled screen" )or App_is_over:
            restart()
            # 重新travel到目前畫面，從最近的screen 0開始跑!!!!!!!!!!!!!!!!!!!
            # os.system("adb shell input tap "+str(travel[1]))
            # print("adb shell input keyevent 4 ")
            # os.system("adb shell input keyevent 4 ")
            # if xmlScreenCompare(screen_list)[1]==current_screen_number:
            #     next_screen_number=xmlScreenCompare(screen_list)[1]
            #     return (next_screen_number,root)
            # else:             
            for travel in NodeList[current_screen_number].toScreenInputs:
            # os.system("adb shell input tap "+str(coordinateList[target][0])+" "+str(coordinateList[target][1]))                     
                time.sleep(1)
                os.system("adb shell input tap "+str(travel[1]))
                print("travel back..................................... "+str(travel[1]))
                try:
                      time.sleep(1)
                      os.system("adb shell /system/bin/uiautomator dump /data/Screen_xml_info.xml")
                      os.system("adb pull /data/Screen_xml_info.xml ")
                      tree = ET.parse('Screen_xml_info.xml')
                      root = tree.getroot()
                except:
                      input("After screen loaded press Enter to continue...")
                      os.system("adb shell /system/bin/uiautomator dump /data/Screen_xml_info.xml")
                      os.system("adb pull /data/Screen_xml_info.xml ")
                      tree = ET.parse('Screen_xml_info.xml')
                      root = tree.getroot()

            next_screen_number=current_screen_number
            # print(" All_traveled_path[-1][0]: "+str(next_screen_number))
            return (next_screen_number,root)             



    return (next_screen_number,root)






    



screen_number=-1
picture_screen_number=0
node=0
root=0



def xmlScreenCompare(screen_list):
    global screen_number
    global picture_screen_number
    global screen_button_tap
    global node
    global NodeList
    global All_traveled_path
    global root
    global FrameLayoutList

    try:
        time.sleep(1)
        os.system("adb shell /system/bin/uiautomator dump /data/Screen_xml_info.xml")
        os.system("adb pull /data/Screen_xml_info.xml ")
        tree = ET.parse('Screen_xml_info.xml')
        root = tree.getroot()
    except:
        input("After screen loaded press Enter to continue...")
        os.system("adb shell /system/bin/uiautomator dump /data/Screen_xml_info.xml")
        os.system("adb pull /data/Screen_xml_info.xml ")
        tree = ET.parse('Screen_xml_info.xml')
        root = tree.getroot()



    
    screen_XML_load=open('Screen_xml_info.xml','r')
    screen_XML_info=screen_XML_load.read()
    screen_XML_load.close()
    screen_XML_tree=tree
    
    # 把每個畫面的xml截取下來
    screen_XML_write=open(("./"+packageName+"_ScreenCaputure/screen"+str(picture_screen_number)+".xml"),'w')
    screen_XML_write.write(str(screen_XML_info))
    screen_XML_write.close
    

    # 把每個畫面的螢幕截圖截取下來
    os.system("adb shell screencap -p /sdcard/screen"+str(picture_screen_number)+".png")
    os.system("adb pull /sdcard/screen"+str(picture_screen_number)+".png"+" ./"+packageName+"_ScreenCaputure/screen"+str(picture_screen_number)+".png")
    os.system("adb shell rm /sdcard/screen"+str(picture_screen_number)+".png")
    
    # print("screen number : "+str(picture_screen_number))
    picture_screen_number+=1
    
    
    # screen_number＝0 代表執行過xmlScreenCompare 以及 最初畫面
    if screen_number==-1:
        screen_number=0
        screen_is_same=False





        
    for XML in screen_list:
        # 是否為同一畫面的意思
        screen_is_same = True

        print("enter XML compare ")
        
        getViews_FrameLayout(root)
        viewList1 = list(FrameLayoutList)
        FrameLayoutList=[]

        viewList2 = getViews_FrameLayout(XML.getroot())
        viewList2 = list(FrameLayoutList)
        FrameLayoutList=[]

        # print(len(viewList1))
        size1 = len(viewList1)
        # print(len(viewList2))
        size2 = len(viewList2)
  
        # view list大小不一樣就直接判斷不一樣，不能這樣，同一個畫面有可能只是list數量不一樣，這邊的view list變成只有class =="android.widget.FrameLayout" 
        if size1 != size2:
            print("size is different so screen_is_not_same")
            screen_is_same=False
        
        else:
            for i in range(0,size1,1):

           
                # 比骨架就好class="android.widget.FrameLayout"
                if  viewList1[i].attrib["class"]=="android.widget.FrameLayout" and viewList2[i].attrib["class"]=="android.widget.FrameLayout":
                        bounds=(viewList1[i].attrib["bounds"]==viewList2[i].attrib["bounds"])
                        index = (viewList1[i].attrib["index"]==viewList2[i].attrib["index"])
                        text = (viewList1[i].attrib["text"]==viewList2[i].attrib["text"])
                        resource_id = (viewList1[i].attrib["resource-id"]==viewList2[i].attrib["resource-id"])
                        Class = (viewList1[i].attrib["class"]==viewList2[i].attrib["class"])
                        package = (viewList1[i].attrib["package"]==viewList2[i].attrib["package"])
                        content_desc = (viewList1[i].attrib["content-desc"]==viewList2[i].attrib["content-desc"])
                        checkable = (viewList1[i].attrib["checkable"]==viewList2[i].attrib["checkable"])
                        checked = (viewList1[i].attrib["checked"]==viewList2[i].attrib["checked"])
                        clickable = (viewList1[i].attrib["clickable"]==viewList2[i].attrib["clickable"])
                        enabled = (viewList1[i].attrib["enabled"]==viewList2[i].attrib["enabled"])
                        focusable = (viewList1[i].attrib["focusable"]==viewList2[i].attrib["focusable"])
                        focused = (viewList1[i].attrib["focused"]==viewList2[i].attrib["focused"])
                        scrollable = (viewList1[i].attrib["scrollable"]==viewList2[i].attrib["scrollable"])
                        long_clickable = (viewList1[i].attrib["long-clickable"]==viewList2[i].attrib["long-clickable"])
                        password = (viewList1[i].attrib["password"]==viewList2[i].attrib["password"])
                        selected = (viewList1[i].attrib["selected"]==viewList2[i].attrib["selected"])
                        
           
                        if not (bounds and index and text and resource_id and Class and package and content_desc and checkable and checked and clickable and enabled and focusable and focused and scrollable and long_clickable and password and selected):
                        # if not (index and text and resource_id and Class and package and content_desc and checkable and checked and clickable and enabled and focusable and focused and scrollable and long_clickable and password and selected):

                            screen_is_same = False
                            print(" viewList is not screen_is_not_same")
                            break
            screen_index=screen_list.index(XML)
            print("same_screen_index:"+str(screen_index))
            if screen_is_same == True:
                break





   # True means screen is duplicate , False means screen is new
    if screen_is_same:
        return (True,screen_index)
    
    else:
        screen_list.append(screen_XML_tree)
        print("add screen:"+str(len(screen_list)-1)+" xml into screen_list")
        return_number=screen_number
        
        screen_button_tap[screen_number]=[set(),set(),"screen hasn't traveled",collections.OrderedDict()]      
        # print("return_number: "+str(return_number)+" All_traveled_path: "+str(All_traveled_path))
        traveled_path=[]
        tmpnumber=return_number-1
        All_traveled_path.reverse()
        for path in All_traveled_path:
            if path[0]==0:
                traveled_path.append(path)
                break
            if path[0]==tmpnumber:
                traveled_path.append(path)
                tmpnumber-=1
            
        traveled_path.reverse()
        All_traveled_path.reverse()
        print("Add toScreen path to Node"+str(return_number)+":"+str(traveled_path))
        
        node = Node(return_number,list(traveled_path))

        NodeList.append(node) 

        # edge = Edge((return_number,))
        # 畫面結構的初始化
        screen_number+=1
        return (False,return_number)








def tapByCoverageRate(NodeList,screen_button_tap,folder):
    global step
    global argument
    coverageRateList=[]
    tappedButtons=set()
    for screen_number in screen_button_tap.keys():
        for button,rate in screen_button_tap[screen_number][3].items():
            coverageRateList.append((screen_number,(button,rate)))
    coverageRateList.sort(key=lambda tup: tup[1][1])
    print(str(coverageRateList))

    for button in coverageRateList:
        if (str(button[0]),str(button[1][0]))  not in tappedButtons:
            if 0<float(button[1][1])<1:
                restart()
                print("Target button:"+str(button))

                for travel in NodeList[button[0]].toScreenInputs:
                # button[0]為到button所在的畫面
                # os.system("adb shell input tap "+str(coordinateList[target][0])+" "+str(coordinateList[target][1]))                     
                    time.sleep(1)
                    tappedButtons.add((str(travel[0]),str(travel[1])))
                    print("     tap button:"+str(travel[0]),str(travel[1]))
                    os.system("adb shell input tap "+str(travel[1]))
                    step+=1

                    print("step: "+str(step))

                os.system("adb shell input tap "+str(button[1][0]))
                print("     last tap button:"+str(button))
                tappedButtons.add((str(button[0]),str(button[1][0])))
                print("        tappedButtons: "+str(tappedButtons))

                os.system("adb -d logcat -d System.out:I *:S > templog.txt")
                logfile=open("templog.txt",'r')        
                stepfile=open(os.path.join(folder,"step"+str(step)+"_additional_covered_line.txt"),'w')
                stepfile.write(logfile.read())
                logfile.close()
                stepfile.close()

                CoverageRate,Button_covered_classes,button_coverage_rate=Coverage.main(argument,step)
                # 不一定每次button的coverage都一樣，要用累進而不是最新的來處理 
                
                print("Current total Coverage rate :"+str(CoverageRate)+"%")

                step+=1
                print("step: "+str(step))







# check traveled graph model structure
def final_model_structure(NodeList,EdgeList,screen_number,screen_button_tap):   
    print("Node List : "+str(NodeList))
    for Node_info in NodeList:
        print("     Node "+str(Node_info.identifier)+": ")
        print("             inEdges :"+str(Node_info.inEdges)+"  ")
        print("             outEdges :"+str(Node_info.outEdges)+"  ")

    print("Edge List : "+str(EdgeList))
    for Edge_info in EdgeList:
        print("         "+str(Edge_info.identifier)+" , ", end="")
        print("         "+str(Edge_info.button_tapped)+" . ")
    print("screen_button_all and screen_button_tapped: ")   

    for screen_number in screen_button_tap.keys():
        print("     screen: "+str(screen_number))
        print("             button_tapped: "+str(screen_button_tap[screen_number][0]))
        tmp=collections.OrderedDict(sorted(screen_button_tap[screen_number][3].items(),key=lambda item: item[1]))
        for  a,b in tmp.items():
            tmp.update({a:str(b*100)+" %"})
        print("             button_tapped_coverage_rate: "+str(tmp))
        print("             button_all: "+str(screen_button_tap[screen_number][1]))
        for node in NodeList:
            if node.identifier==screen_number:
                print("                     to_screen_path: "+str(node.toScreenInputs)) 




screen_list=[]
step=-1
def generateTrace(traceLength,packageName,folder):
    global screen_list
    global screen_number
    global NodeList
    global button_tapped
    global All_traveled_path
    global screen_button_tap
    global root
    global step
    global argument

    current_screen_number=0
    next_screen_number=0


   
    step=0
    while step<traceLength:
        
        # 初始化，先建initial畫面的node
        if step ==0:
            print("-------------------------------------------------------------")
            print("Initialize..........")
            print("current_screen_number: "+str(current_screen_number))            
            print("next_screen_number: "+str(xmlScreenCompare(screen_list)[1]))
            CoverageRate,Button_covered_classes,button_coverage_rate=Coverage.main(argument,-1)
            print("Current total Coverage rate :"+str(CoverageRate)+"%")

        print("-------------------------------------------------------------")
        print("Step: "+str(step))


        # cmd line會回報時間
        os.system("adb logcat -c")        
        (next_screen_number,root)=ripping(root,next_screen_number)
        print("tapping....................")
        time.sleep(1)
        past_screen_button_tap=dict(screen_button_tap)
        

        # Node產生
        All_traveled_path.append((next_screen_number,button_tapped))

        # traveled_path[next_screen_number]=button_tapped

        print("All_traveled_path: "+str(All_traveled_path))
        # print("traveled_path: "+str(traveled_path))

        current_screen_number=next_screen_number
        # print("current_screen_number: "+str(current_screen_number))
        next_screen_number=xmlScreenCompare(screen_list)[1]
        print("next_screen_number: "+str(next_screen_number))



       
        
        
        #建model     
        
        EdgeList.append(Edge((current_screen_number,next_screen_number), current_screen_number, next_screen_number,button_tapped=button_tapped))
        NodeList[next_screen_number].add_inEdge((current_screen_number,next_screen_number))
        NodeList[current_screen_number].add_outEdge((current_screen_number,next_screen_number),button_tapped)

       

        os.system("adb -d logcat -d System.out:I *:S > templog.txt")
        # os.system("adb -d logcat -v time  -d System.out:I *:S > templog.txt")
        logfile=open("templog.txt",'r')
        
        stepfile=open(os.path.join(folder,"step"+str(step)+"_covered_line.txt"),'w')
        stepfile.write("Screen"+str(next_screen_number)+"\n")
      
        stepfile.write(logfile.read())
      
        logfile.close()
        stepfile.close()
        
        CoverageRate,Button_covered_classes,button_coverage_rate=Coverage.main(argument,step)
        # 不一定每次button的coverage都一樣，用最新的來處理
        screen_button_tap[current_screen_number][3].update({button_tapped:button_coverage_rate})
        print("Current total Coverage rate :"+str(CoverageRate)+"%")




        if step >1:
            (next_screen_number,root)=check_problem(past_screen_button_tap,current_screen_number,next_screen_number,root,All_traveled_path)
            

        step=step+1


        print("-------------------------------------------------------------")













def restart():
        os.system("adb shell am force-stop "+packageName)
        os.system("adb logcat -c")        
        os.system("adb shell am start "+packageName+"/"+activityName)
        time.sleep(1)



# package name and activity name of SUT 
# 之後設定成讓使用者自行輸入的參數
# packageName="org.tomdroid"
# activityName=".ui.Tomdroid" 
appName="tomdroid-0.7.5.apk"  
packageName="org.tomdroid"
activityName=".ui.Tomdroid"  

folder=""
argument=""

def main():
    global packageName
    global activityName
    global screen_number
    global folder
    global argument
    # first use need to get authority of folder
    # os.system("sudo chmod -R 777 .")
    os.system("adb root")

    trace=0
    while trace<1:
        restart()

        if(os.path.isdir("Trace")==0):
            os.mkdir("Trace")
        folder="Trace/trace"+str(trace)
        # 放screen 那邊的資料夾重複也刪掉
        if(os.path.isdir(folder)):
            shutil.rmtree(folder)
        os.mkdir(folder)

        screen_picture_and_xml_folder=str(packageName+"_ScreenCaputure")
        if(os.path.isdir(screen_picture_and_xml_folder)):
            shutil.rmtree(screen_picture_and_xml_folder)
        os.mkdir(screen_picture_and_xml_folder)

        ddx_folder=str(appName.replace(".apk","")+"_ddx")
        if(os.path.isdir("sootOutput/"+str(ddx_folder))):
            pass
        else:
            os.system("unar sootOutput/"+str(appName)+" -o sootOutput/")
            os.system("java -jar ddx1.26.jar  -d sootOutput/"+str(appName.replace(".apk",""))+"_ddx sootOutput/"+str(appName.replace(".apk",""))+"/classes.dex")
        argument=['Testcase_Coverage_ver2.py', "./sootOutput/"+str(appName.replace(".apk",""))+"_ddx/"+packageName.replace(".","/")+"/", './Trace/']




        # initial executed line number info
        os.system("adb -d logcat -d System.out:I *:S > templog.txt")       
        logfile=codecs.open("templog.txt",'r')
        stepfile=codecs.open(os.path.join(folder,"Initial_covered_line.txt"),'w')
        stepfile.write(logfile.read())
        logfile.close()
        stepfile.close()
        

        
        generateTrace(60,packageName,"Trace/trace"+str(trace))
        # testing
        final_model_structure(NodeList,EdgeList,screen_number,screen_button_tap)
        input("After configure the setting press Enter to continue...")
        tapByCoverageRate(NodeList,screen_button_tap,folder) 


        trace=trace+1
    
    os.system("adb shell am force-stop "+packageName)
    final_model_structure(NodeList,EdgeList,screen_number,screen_button_tap)
    exit()



# program entry point
if __name__ == "__main__":
    main()
