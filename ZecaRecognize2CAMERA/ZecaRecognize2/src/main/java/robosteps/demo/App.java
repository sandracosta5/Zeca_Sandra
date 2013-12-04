/*Relatório: Este programa como está é o teste piloto para a atividade recognize.
 * Guarda info sobre crianças, sessoes, performance e respostas, mas não cria ficheiro final com a junção de vários documentos
 * não permite a repetição de emoções através de cartão e pode acontecer de a mesma expressão ser mostrada duas vezes seguidas
 * Único problema é a msg de erro quando fala o reforço. Algum tipo de erro, por voltar a chamar IP (settings()), mas que não consegui resolver
 * 
 * Cartão Repeat, STOP e Start OK
 * A mesma expressão ser mostrada duas vezes seguidas - OK
 * Tempo de execução 5 minutos
 */

package robosteps.demo;

/**
 * Main
 */
import org.robokind.api.animation.Animation;
import com.robosteps.api.core.*;
import robosteps.Tasks.ActivityType;
import friendularity.test.camera.r50.ImageDemo;
import friendularity.test.camera.r50.ImageMonitor;
import java.io.File;
import java.io.FileNotFoundException;
import robosteps.demo.expdata.Child;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static robosteps.demo.App.NUM_EMOTIONS;
import robosteps.demo.expdata.Reinforcement;
import robosteps.sessions.Answer;
import robosteps.sessions.Performance;
import robosteps.sessions.Session;

/**
 * @author Sandra Costa - scosta@dei.uminho.pt
 */

public class App implements Runnable{
    private static RsRobot myRobot;
    public ImageDemo iKey;
    public Thread tThread;
    public static final int NUM_EMOTIONS = 5;
    public ImageMonitor monit = new ImageMonitor();
    public String childCode = "";
    public String instructionCode = "";
    public String emotionCode = "";
    public String activityCode = "";
    public Admin admin = new Admin ();
    public Child child = new Child ();
    public String IP = "192.168.1.108";
    

    Animation smile = Robosteps.loadAnimation("V8/happyHead");
    Animation blink = Robosteps.loadAnimation("V8/blink");
    Animation greeting = Robosteps.loadAnimation("V8/01");
    Animation anim1 = Robosteps.loadAnimation("V8/02");
    Animation anim2 = Robosteps.loadAnimation("V8/03");
    Animation anim3 = Robosteps.loadAnimation("V8/04");
    Animation emotionOut = Robosteps.loadAnimation("V8/04");
    Card Fear = new Card ("Fear", "V8/afraidHeadGesture", Emotion.AFRAID);
    Card Anger = new Card ("Anger", "V8/angryHeadGesture", Emotion.ANGRY);
    Card Joy = new Card ("Joy", "V8/happyHeadGesture", Emotion.HAPPY);
    Card Sadness = new Card ("Sadness", "V8/sadHeadGesture", Emotion.SAD);
    Card Surprise = new Card ("Surprise", "V8/surprisedHeadGesture", Emotion.SURPRISED);
    Card emotionCard = new Card();
    boolean firstTime = true;
    int repeat = 0;
    boolean proceed = true;
    
    /**
     * Creates a Thread so we can pause and interrupt it
     */
    public App(){ 
    	tThread = new Thread(this);  
        tThread.start();
    }
    
    
    /**
     * Notifies all Threads
     */
    public synchronized void wakeUp(){
        synchronized (this) {
            notifyAll();
        }	
    } 
    
    /**
     * Gets QR Code
     * @return 
     */
    public synchronized String getQRCode(){
        ImageDemo.ID = "";
        monit.processOK();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return iKey.getID();   
    }

    /**
     * Main, IP is defined
     * @param args 
     */
    public static void main( String[] args ){
        App myapp = new App();
        /* Definition of Robot's IP*/
        String IP = "192.168.1.108";
        UserSettings.setRobotId("myRobot");
        UserSettings.setRobotAddress(IP);
        UserSettings.setSpeechAddress(IP);
        UserSettings.setAnimationAddress(IP); 
    }
    
    /**
     * Thread. Calls startZeca()
     */
    @Override
    public void run(){
    	startZeca();
    }
    
    /**
     * Verifies if a child exists using a code (String) coming from the QR Code 
     * @param code
     * @return true if the child exists
     */
    public static boolean childExists(String code){
         
        boolean check = false;
        File dir = new File("./ChildrenData/");
        String[] dirChildren = dir.list(); 

        //Children in the file
        for(int i=0; i<dirChildren.length;i++) {
            if(Integer.parseInt(code) == i+1){
                check = true; // child exists
                break;
            }      
            else 
                check = false; // child does not exists
        }

        return check;
    }

    /**
     * Main method
     */
    public void startZeca( ){
        //Gets camera
        iKey = new ImageDemo(this);
        //to get if it is a number
        boolean num= false;
        Scanner input = new Scanner(System.in); 
        //connects the robot
        myRobot = Robosteps.connectRobot();      
        myRobot.speak("Olá Sandra! Vamos começar a trabalhar.");
        myRobot.playAnimation(anim3);
        
        //Participant's identification using QR CODE (Verifies if the child is is the system)
        do{
            myRobot.speak("Quem é o participante?");
            Robosteps.sleep(3000);
            //
            childCode = getQRCode();
            //childCode = "01";
            //childCode = input.nextLine();
            System.out.println("O que sou eu?"+childCode);
            //Checks if the QR Code shows a Child's Code (eg. 02)
            num = isNumeric(childCode);
            //Asks again if the QR Code does not correspond to a Child's Code or the child does not exist
        }while((num == false) || (!childExists(childCode) ) );

        
        //Accessing Child's Data
        Child myChild = new Child(Integer.parseInt(childCode));
        String myChildName = myChild.getNameChild();
        Reinforcement myChildReinforcement = myChild.getReinforcementChild();
        //Gets the total # of sessions this child did already
        int myChildNSessions = myChild.getNSessions();
        myRobot.speak("O participante chama-se " + myChildName + ".");
        Robosteps.sleep(500);
        
        if(!"".equals(childCode)){
            //Waits 3 seconds for next QR Coce
            Time timerAfterChildID = new Time(3); //Seconds
            while(timerAfterChildID.flag == false){
                monit.processSTOPS();
            }
            //3 seconds are gone
            monit.processOK();
        }
        
        //Activity chosen is Recognize
        activityCode = "RECOGNIZE";
        String nomeAtividade = "Reconhecer";
        ActivityType currentActivityType = ActivityType.RECOGNIZE;
                
        myRobot.speak("Vamos jogar ao jogo " +nomeAtividade+".");
        myRobot.speak("Mostra o cartão START para começarmos.");
        Robosteps.sleep(5000);
        
        //Waits for START instruction 
        do{
        instructionCode = getQRCode();
        //instructionCode = input.nextLine();
        }while ((!"START".equals(instructionCode)) );

        //Start a new Session
        myRobot.playAnimation(greeting);
        //myRobot.speak("Olá. Que bom te ver. Vamos jogar ao jogo Reconhecer.");
        myRobot.speak("Olá " + myChildName + ". Que bom te ver. Vamos jogar ao jogo Reconhecer.");
        Robosteps.sleep(3000);
        myRobot.playAnimation(anim3);
        myRobot.speak("Neste jogo tens que escolher a ráquééte que mostra o que eu estou a sentir. Vamos lá?");
        Robosteps.sleep(10000);
        
        //Updates number of sessions of the child
        myChildNSessions++; 
        //Sets number of sessions of the child
        myChild.setNSessions(myChildNSessions);
        //Saves child's info 
        myChild.saveChildData(); 
        //Creates a new session
        Session newSession = new Session(); 
        //Creates a new session folder, and files
        newSession.createDataStructureSessions(myChild); 
        //Defines present day and time
        GregorianCalendar now = new GregorianCalendar(); 
        //Sets present date 
        newSession.setDate(now);
        //Sets start time
        newSession.setStartTime(now);
        //Sets activity to be performed
        newSession.setActivity(currentActivityType);
        //Gets code of the session, produced automatically (lastest+1)
        String codeSession = newSession.getCodeSession();
        //Saves session's data 
        newSession.saveSessionData(childCode);  //Saves the inicial data to the files

        //Creates answer and performance
        Answer ans = new Answer ();
        Performance perf = new Performance ();

        //Starts a timer (value in seconds)
        Time timerSession = new Time(350); 
        //When the time is up flag = true
        while((timerSession.flag == false) && (!"STOP".equals(emotionCode))){
            
        //Defines code of the answer
        ans.setCodeAnswer(Integer.toString(perf.numAnswers())); 
        //Time of the prompt
        GregorianCalendar prompt = new GregorianCalendar();
        Robosteps.sleep(3000);
        //Gets prompt
        String randEm = randEmotion();
        emotionOut = Robosteps.loadAnimation(randEm);
        //The emotion shown by the robot is saved as input
        ans.setInput(emotionCard.getCode());

        //Asks the child to choose the correct card
        randInstruction();
        do{
        //Shows the emotion
        Robosteps.sleep(1000);
        myRobot.playAnimation(emotionOut);
        Robosteps.sleep(3000);
        
        //Receives String form QR Code
        emotionCode = getQRCode();
            
       /* int option;
        option = input.nextInt();
        if(option == 1)emotionCode = "Fear";
        else if (option == 2)emotionCode ="Joy";
        else if (option == 3) emotionCode = "Sadness";
        else if (option == 4) emotionCode = "Anger";
        else emotionCode = "Surprise";
        */
        }while("REPEAT".equals(emotionCode));
        
       
        //Defines answer from the child
        ans.setOutput(emotionCode); 
        //Time of the answer was shown
        GregorianCalendar promptAnswered = new GregorianCalendar(); 
        //Calculates the time between the prompt and the answer  
        ans.calculateResponseTime (prompt, promptAnswered);          
        //Verifies if the answer is right or wrong (IF Stop Card is show it does not show wrong reinforcement
        if(!"STOP".equals(emotionCode))ans.matching(myChildReinforcement); 
        //Insert answer in the TreeMap
        perf.insertAnswer(ans); 
        //Calculates if the answer is wrong or right, and updates counter
        perf.calculatePerformance(ans);
        //Increments codeAnswer
        int c = Integer.parseInt(ans.getCodeAnswer());
        c++;
        ans.setCodeAnswer(Integer.toString(c));
        //Saves data from the child's performance
        perf.saveSessionPerformanceData(newSession, childCode, codeSession);
        //Saves data from the child's session
        newSession.saveSessionData(childCode);
        
        //Saves answers in the file
        try {
            ans.saveAnswerData(childCode, codeSession);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
        
        //End of the session
        //Defines time the session ended
        GregorianCalendar later = new GregorianCalendar(); 
        //Save this time as endTime
        newSession.setEndTime(later); 
        //Calculates duration of the session
        float duration = later.getTimeInMillis() - now.getTimeInMillis();       
        duration = duration/1000;
        newSession.setDuration(duration);
        //Saves data from the child's session
        newSession.saveSessionData(childCode);
              
        //Closes camera
        iKey.runit=false; 
        //Farewell
        myRobot.playAnimation(anim2);
        myRobot.speak("O nosso jogo terminou por hoje. Foi muito bom ter jogado contigo. Até breve.");
        Robosteps.sleep(5000);
        
        Robosteps.disconnect();
        System.exit(0);   
    }
    
    /**
     * 
     * @param i - limit of the random method
     * @return int random
     */
    public int randNum(int i){
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(i);
        
        randomInt +=1; 
        return randomInt;
    }
    
    /**
     * Randomizes the emotion to be presented to the child
     * Prevents repeating the same expressions twice in a row
     * @return String with address of the emotion
     */
     public String randEmotion(){
        int j = 0;
        do{
            j = randNum(NUM_EMOTIONS);
        }while (j == repeat);
        String result = "";
        if(j == 1) {emotionCard = Fear; repeat = 1;}
        else if (j == 2) {emotionCard = Anger; repeat = 2;}
        else if (j == 3) {emotionCard = Joy; repeat = 3;}
        else if (j == 4) {emotionCard = Sadness; repeat = 4;}
        else if (j == 5) {emotionCard = Surprise; repeat = 5;}
         System.out.println(j);
        result = emotionCard.getAddress();
        return result;     
     }
     
     public String randInstruction(){
        int j = randNum(4);
        String result = "";
        if(firstTime == true) {myRobot.speak("Escolhe a resposta certa pegando na ráquéete."); j=0;}
        if(j == 1) myRobot.speak("Qual é a resposta certa?");
        else if (j == 2) myRobot.speak("Escolhe a resposta certa pegando na ráquéete.");
        else if (j == 3) myRobot.speak("Qual é a imagem certa?");
        else if (j == 4) myRobot.speak("Escolhe a imagem correta.");
        myRobot.playAnimation(blink);
        Robosteps.sleep(3000);
        firstTime = false;
        return result;     
    }
     
     /**
      * Checks if a String is numeric
      * @param number String to tested
      * @return true if the String is numberic
      */
     public static boolean isNumeric(String number){  
         boolean isValid = false;
           String expression = "[-+]?[0-9]*\\.?[0-9]+$";  
           CharSequence inputStr = number;  
           Pattern pattern = Pattern.compile(expression);  
           Matcher matcher = pattern.matcher(inputStr);  
           if(matcher.matches()){  
              isValid = true;  
           }  
           return isValid;  
         }  
}
