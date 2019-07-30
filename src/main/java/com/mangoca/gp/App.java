package com.mangoca.gp;

import com.mangoca.gp.worker.IpNetworkStatisticsWorker;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by Ashfakur rahman
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        JobDetail job = JobBuilder.newJob(IpNetworkStatisticsWorker.class)
                        .withIdentity("ip statistics report builder","ping_stat")
                        .build();
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("every hour ping", "ping_stat")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 0 0/5 1/1 * ? *"))
                .build();

        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        scheduler.scheduleJob(job,trigger);



    }
}
