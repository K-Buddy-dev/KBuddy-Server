package com.example.kbuddy_backend.auth.service;

import com.example.kbuddy_backend.common.util.RedisUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Random;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailSendService {

	private final JavaMailSender mailSender;
	private final RedisUtil redisUtil;
	private int authNumber;

	//임의의 6자리 양수를 반환
	public void makeRandomNumber() {
		Random r = new Random();
		StringBuilder randomNumber = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			randomNumber.append(Integer.toString(r.nextInt(10)));
		}

		authNumber = Integer.parseInt(randomNumber.toString());
	}

	public boolean CheckAuthNum(String email, String authNum) {
		if (redisUtil.getData(authNum) == null) {
			return false;
		} else
			return redisUtil.getData(authNum).equals(email);
	}

	//mail을 어디서 보내는지, 어디로 보내는지 , 인증 번호를 html 형식으로 어떻게 보내는지 작성.
	public String joinEmail(String email) {
		makeRandomNumber();
		String setFrom = "officialkbuddy@gmail.com";
		String title = "AUTH CODE for K-Buddy Registration"; // 이메일 제목
		String content =
			"Welcome to K-Buddy" +    //html 형식으로 작성 !
				"<br><br>" +
				"Auth number is " + authNumber + "." +
				"<br>" +
				"Please write a correct auth code."; //이메일 내용 삽입
		mailSend(setFrom, email, title, content);
		return Integer.toString(authNumber);
	}

	//이메일을 전송합니다.
	public void mailSend(String setFrom, String toMail, String title, String content) {
		MimeMessage message = mailSender.createMimeMessage();//JavaMailSender 객체를 사용하여 MimeMessage 객체를 생성
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");//이메일 메시지와 관련된 설정을 수행합니다.
			// true를 전달하여 multipart 형식의 메시지를 지원하고, "utf-8"을 전달하여 문자 인코딩을 설정
			helper.setFrom(setFrom);//이메일의 발신자 주소 설정
			helper.setTo(toMail);//이메일의 수신자 주소 설정
			helper.setSubject(title);//이메일의 제목을 설정
			helper.setText(content, true);//이메일의 내용 설정 두 번째 매개 변수에 true를 설정하여 html 설정으로한다.
			mailSender.send(message);
		} catch (MessagingException e) {//이메일 서버에 연결할 수 없거나, 잘못된 이메일 주소를 사용하거나, 인증 오류가 발생하는 등 오류

			log.error("이메일 전송 에러 발생", e);
		}
		//인증 코드는 5분간 유효
		redisUtil.setDataExpire(Integer.toString(authNumber), toMail, 60 * 5L);

	}

}