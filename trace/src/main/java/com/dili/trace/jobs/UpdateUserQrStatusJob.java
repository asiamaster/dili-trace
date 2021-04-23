package com.dili.trace.jobs;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.dili.trace.dto.query.UserQrHistoryQueryDto;
import com.dili.trace.service.RegisterBillService;

import com.dili.trace.service.UserQrHistoryService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserQrStatusJob implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(UpdateUserQrStatusJob.class);
    @Autowired
    RegisterBillService billService;
    @Autowired
    UserQrHistoryService userQrHistoryService;

    @Scheduled(cron = "0 0/2 * * * ?")
    public void execute() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = this.start(now);
        LocalDateTime end = this.end(now);
        logger.info("开始执行任务: 根据 {}-{} 之内报备单数据更新用户颜色码",start.format(DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss")),end.format(DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss")) );
        try {
            UserQrHistoryQueryDto historyQueryDto=new UserQrHistoryQueryDto();
            historyQueryDto.setCreatedStart(start);
            historyQueryDto.setCreatedEnd(end);
            this.userQrHistoryService.updateUserQrForExpire(historyQueryDto);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        this.execute();

    }

    protected LocalDateTime start(LocalDateTime now) {
        LocalDateTime start = now.minusDays(6);
        return start;
    }

    protected LocalDateTime end(LocalDateTime now) {
        LocalDateTime end =now;
        return end;
    }

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        UpdateUserQrStatusJob job = new UpdateUserQrStatusJob();
        LocalDateTime start = job.start(now);
        LocalDateTime end = job.end(now);
        logger.info("{}-{}",start,end);
    }

}