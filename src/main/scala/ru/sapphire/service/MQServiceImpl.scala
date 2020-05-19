package ru.sapphire.service

import com.ibm.mq.constants.CMQC
import com.ibm.mq.{MQEnvironment, MQMessage, MQPutMessageOptions, MQQueueManager}
import monix.eval.Task


object MQServiceImpl{
  MQEnvironment.hostname = MQ_HOST
  MQEnvironment.port     = MQ_PORT
  MQEnvironment.channel  = MQ_CHANNEL
  private val qMgr1F: Task[MQQueueManager] = Task {new MQQueueManager(MQ_QMANAGER_NAME)}
  private val sendMsgF: Task[MQMessage] = Task {new MQMessage}
  private val mqPutMessageOptions: Task[MQPutMessageOptions] = Task(new MQPutMessageOptions)
  private val sendOpt: Int = CMQC.MQOO_OUTPUT

  val monexTaskAddForMQ: (String, String) => Task[Unit] = (body, topik) => for {
    qMgr1   <- qMgr1F
    sendMsg <- sendMsgF
    mqop    <- mqPutMessageOptions
    queue1  = qMgr1.accessQueue(topik, sendOpt)
    _       = sendMsg.format = CMQC.WMQ_MESSAGE_BODY
    msgb = {
      if (body.equalsIgnoreCase("ISO-8859-5")) {
        sendMsg.characterSet = 915
        body.getBytes("8859_5")
      } else {
        sendMsg.characterSet = 1208
        body.getBytes("UTF-8")
      }
    }
    _ = sendMsg.write(msgb)
    _ = queue1.put(sendMsg, mqop)
    _ = queue1.close()
    _ = qMgr1.disconnect()
  } yield ()

}
