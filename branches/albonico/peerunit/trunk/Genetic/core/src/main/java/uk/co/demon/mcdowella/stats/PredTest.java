package uk.co.demon.mcdowella.stats;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

// Test out different types of prediction

class PredResult
{
  int tries=0;
  int fails=0;
  int low=0;
  int high=0;
  int equal=0;
  int inside=0;
  int outside=0;
  double sumErr=0.0;
  double sumSqErr=0.0;
  double sumLen=0.0;
  
  public void observe(double result, boolean ok,
    double prediction, double lowP, double highP)
  {
    tries++;
    if(!ok)
    {
      fails++;
      return;
    }
    if(result<prediction)
    {
      low++;
    }
    else if(result>prediction)
    {
      high++;
    }
    else
    {
      equal++;
    }
    if(result>=lowP && result<=highP)
      inside++;
    else
      outside++;
    double error=prediction-result;
    sumErr+=error;
    sumSqErr+=error*error;
    sumLen+=highP-lowP;    
  }
  
  public int getTries() {return tries;}
  public int getFails() {return fails;}
  public int getLow() {return low;}
  public int getHigh() {return high;}
  public int getEqual() {return equal;}
  public int getInside() {return inside;}
  public int getOutside() {return outside;}
  public double meanErr()
  {
    if(sumErr==0.0)
      return sumErr;
    return sumErr/(tries-fails);
  }
  public double meanSqErr()
  {
    if(sumSqErr==0.0)
      return sumSqErr;
    return sumSqErr/(tries-fails);
  }
  public double meanSize()
  {
    if(sumLen==0.0)
      return sumLen;
    return sumLen/(tries-fails);
  }
  public String toString()
  {
    StringBuffer b=new StringBuffer("Tried "+getTries()+
      " succeeded "+(getTries()-getFails())+
      " failed "+getFails()+'\n');
    b.append("Low "+getLow()+" equal "+getEqual()+" high "+getHigh()+'\n');
    b.append("Inside "+getInside()+" outside "+getOutside()+'\n');
    b.append("Mean error "+meanErr()+" Mean squared error "+meanSqErr()+'\n');
    b.append("Mean size is "+meanSize());
    return b.toString();
  }
}

class PredTest
{
  public static void main(String[] args)
  {
    int goes=1000;
    int seed=99;
    int samples=10;
    double coverage=0.5;
    double noise=0.1;
    double err=1.0e-6;
    int iters=5000;
    int predRank,predRank2,lowRank,highRank,highRank2;
    boolean trouble=false;
    boolean normal=false;
    boolean exp=false;
    PrintStream ps=null;
    for(int i=0;i<args.length;i++)
    {
      if(i<(args.length-1) && args[i].equals("-iters"))
      {
        i++;
        try
        {
          iters=(new Integer(args[i].trim()).intValue());
        }
        catch (NumberFormatException e)
        {
          System.out.println("Could not read iters in "+args[i]);
          trouble=true;
        }
      }
      else if(i<(args.length-1) && args[i].equals("-goes"))
      {
        i++;
        try
        {
          goes=(new Integer(args[i].trim()).intValue());
        }
        catch (NumberFormatException e)
        {
          System.out.println("Could not read goes in "+args[i]);
          trouble=true;
        }
      }
      else if(i<(args.length-1) && args[i].equals("-seed"))
      {
        i++;
        try
        {
          seed=(new Integer(args[i].trim()).intValue());
        }
        catch (NumberFormatException e)
        {
          System.out.println("Could not read seed in "+args[i]);
          trouble=true;
        }
      }
      else if(i<(args.length-1) && args[i].equals("-samples"))
      {
        i++;
        try
        {
          samples=(new Integer(args[i].trim()).intValue());
        }
        catch (NumberFormatException e)
        {
          System.out.println("Could not read seed in "+args[i]);
          trouble=true;
        }
      }
      else if(i<(args.length-1) && args[i].equals("-output"))
      {
        i++;
        try
        {
          ps=new PrintStream(new BufferedOutputStream(new
             FileOutputStream(args[i])),false);
        }
        catch (IOException e)
        {
          System.out.println("Could not open "+args[i]);
          trouble=true;
        }
      }
      else if (args[i].equals("-normal"))
      {
        normal=true;
      }
      else if (args[i].equals("-exp"))
      {
        exp=true;
      }
      else
      {
        System.out.println("Could not handle flag "+args[i]);
        trouble=true;
      }
    }
    if (trouble)
    {
      System.out.println("Usage is PredTest [-iters #] [-goes #] [-seed #] [-samples #] [-output <filename>] [-normal] [-exp]");
      System.exit(1);
    }
     
    System.out.println("Seed is "+seed);
    System.out.println("Samples are "+samples);
    if(ps!=null)
    {
      ps.println("# PredTest diff, diff^2, 50% width");
      ps.println("# For rank-based, linear, leave-out-1");
      ps.println("# Seed is "+seed+" using "+samples+" samples");
    }

    // For rank based predictor, we need three ranks
    // If we have N samples, we can require that our predicted value be such that its
    // error has the same value as the error with rank x in the N samples, so x lies
    // between 0 and N-1. This means that if the predicted point is exchangeable with
    // the sampled point, the true value will fall below the predicted value with
    // probability at most (x+1)/(N+1) and above the predicted value with probability
    // at most (N-x)/(N+1). 
    // middle rank should give best guess answer, so we want rank samples/2. But
    // samples may not be odd, in which case we average two values.
    predRank=(samples-1)/2;
    predRank2=samples/2;
    // Position the other two to contain about half of the
    // samples+1 slots available
    lowRank=samples/4;
    highRank=lowRank+(samples+1)/2;
    highRank2=lowRank+(samples+2)/2;
    
    double x[]=new double[samples+1];
    double y[]=new double[samples+1];
    Random r=new Random(seed);
    PredResult ranker=new PredResult();
    PredResult linear=new PredResult();
    PredResult leave=new PredResult();
    for(int i=0;i<goes;i++)
    {
      double m=r.nextDouble();
      double c=r.nextDouble();
      if(normal)
      {
        for(int j=0;j<=samples;j++)
        {
          x[j]=r.nextGaussian();
          y[j]=x[j]*m+c+r.nextGaussian()*noise;
        }
      }
      else if(exp)
      {
        for(int j=0;j<=samples;j++)
        {
          x[j]= -Math.log(r.nextDouble());
          y[j]=x[j]*m+c-Math.log(r.nextDouble())*noise;
        }
      }
      {
        for(int j=0;j<=samples;j++)
        {
          x[j]=r.nextDouble();
          y[j]=x[j]*m+c+r.nextDouble()*noise;
        }
      }
      // Run rank-based predictor
      CorPredictor rMid=new CorPredictor(x[samples],samples,x,y,
          predRank,predRank2,err,iters);
      CorPredictor rLow=new CorPredictor(x[samples],samples,x,y,
          lowRank,lowRank,err,iters);
      CorPredictor rHigh=new CorPredictor(x[samples],samples,x,y,
          highRank,highRank2,err,iters);
      boolean doPrint=ps!=null;
      double diff,pred,l,h;
      if(rMid.getError()!=null || rLow.getError()!=null || rHigh.getError()!=null)
      {
        ranker.observe(0.0,false,0.0,0.0,0.0);
        doPrint=false;
      }
      else
      {
        pred=rMid.getPrediction();
        l=rLow.getPrediction();
        h=rHigh.getPrediction();
        ranker.observe(y[samples],true,pred,l,h);
        diff=pred-y[samples];
        if (doPrint)
          ps.print("N:"+i+" "+diff+" "+diff*diff+" "+(h-l));
      }
      // Now standard linear (best possible linear) predictor
      Correlation cor=new Correlation(samples,x,y);
      // To get in bounds with probability 1/2, chose .7 sigma, or there abouts
      // Remember not quite normal
      double lErr=Math.sqrt(cor.predYVar(x[samples]))*0.7;
      double lPred=cor.linearY(x[samples]);
      linear.observe(y[samples],true,lPred,lPred-lErr,lPred+lErr);
      if(doPrint)
      {
        diff=lPred-y[samples];
        ps.print(" "+diff+" "+diff*diff+" "+(2.0*lErr));
      }
      // Leave-out-1 version of linear predictor
      LeaveOutPred leavePred=new LeaveOutPred(samples,x,y,x[samples]);
      pred=leavePred.getPred();
      diff=pred-y[samples];
      l=leavePred.getLow();
      h=leavePred.getHigh();
      if(doPrint)
      {
        ps.println(" "+diff+" "+diff*diff+" "+(h-l));
      }
      leave.observe(y[samples],true,pred,l,h);
    }
    if(ps!=null)
    { 
      ps.flush();
      ps.close();
    }
    System.out.println("Rank-based results are\n"+ranker.toString());
    System.out.println("Linear correlation results are\n"+linear.toString());
    System.out.println("Leave-out-1 prediction results are\n"+leave.toString());
  }
};