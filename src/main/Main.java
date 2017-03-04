package main;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ppdai.open.core.AuthInfo;
import com.ppdai.open.core.OpenApiClient;
import com.ppdai.open.core.PropertyObject;
import com.ppdai.open.core.Result;
import com.ppdai.open.core.RsaCryptoHelper;
import com.ppdai.open.core.ValueTypeEnum;

public class Main {

	private static final String AUTO_LOGIN_URL = "http://gw.open.ppdai.com/auth/LoginService/AutoLogin";

	private static final String BIDDING_URL = "http://gw.open.ppdai.com/invest/BidService/Bidding";

	private static final String GET_LOAN_LIST_URL = "http://gw.open.ppdai.com/invest/LLoanInfoService/LoanList";

	private static Logger log = LogManager.getLogger(Main.class.getName());

	private static AuthInfo authInfo = null;

	private static Set<Long> GOT_BIDDING_LISTING_IDS = new HashSet<Long>();

	private static int CONDITION_PAYWAY = 0;

	private static long EACH_AMOUNT = 100;

	private static int TOTAL_AMOUNT = 1000;

	private static int GOT_AMOUNT = 0;

	private static int SLEEP_TIME = 1000;

	private static String APPID = null;
	
	private static String PUBLIC_KEY = null;
	
	private static String PRIVATE_KEY = null;

	private static void loadProperties() throws Exception {
		Properties prop = new Properties();
		InputStream in = null;
		FileInputStream fi = null;
		try {
			fi = new FileInputStream("ppdai.properties");
			in = new BufferedInputStream(fi);
			prop.load(in);
			EACH_AMOUNT = Integer.parseInt(prop.getProperty("EACH_AMOUNT", "100"));
			TOTAL_AMOUNT = Integer.parseInt(prop.getProperty("TOTAL_AMOUNT", "1000"));
			SLEEP_TIME = Integer.parseInt(prop.getProperty("SLEEP_TIME", "1000"));
			APPID = prop.getProperty("APPID");
			PUBLIC_KEY = prop.getProperty("PUBLIC_KEY");
			PRIVATE_KEY = prop.getProperty("PRIVATE_KEY");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					log.info("Failed to close IO");
				}
			}
			if (fi != null) {
				try {
					fi.close();
				} catch (Exception e) {
					log.info("Failed to close IO");
				}

			}
		}
	}

	public static void main(String[] args) {

		try {
			loadProperties();
			log.info("###############################");
			log.info("EACH_AMOUNT: " + EACH_AMOUNT);
			log.info("TOTAL_AMOUNT: " + TOTAL_AMOUNT);
			log.info("SLEEP_TIME: " + SLEEP_TIME);
			log.info("APPID: " + APPID);
			log.info("PUBLIC_KEY: " + PUBLIC_KEY);
			log.info("PRIVATE_KEY: " + PRIVATE_KEY);
			log.info("###############################");
		} catch (Exception e) {
			log.error("Failed to load properties", e);
			return;
		}

		try {
			OpenApiClient.Init(APPID, RsaCryptoHelper.PKCSType.PKCS8, PUBLIC_KEY, PRIVATE_KEY);
			log.info("Got Token>>>>");
			gettoken(args[0]);
			refreshToken();
			log.info("Auto Login>>>>");
			autoLogin();
		} catch (Exception e) {
			log.error("Failed to get token to auto login", e);
			return;
		}

		int pageSize = 0;
		while (GOT_AMOUNT < TOTAL_AMOUNT) {
			try {
				process(++pageSize);
			} catch (Exception e) {
				try {
					log.info("SPENT: " + GOT_AMOUNT);
					Thread.sleep(SLEEP_TIME);
					pageSize = 0;
				} catch (Exception e1) {
					log.error("Failed to sleep", e1);
				}
			}
		}

		log.info("### Your bidding infomation:");
		for (Long id : GOT_BIDDING_LISTING_IDS) {
			log.info("BiddingId: " + id);
		}
		log.info("Finished");
	}

	private static void gettoken(String code) throws Exception {
		authInfo = OpenApiClient.authorize(code);
	}

	private static void refreshToken() throws Exception {
		authInfo = OpenApiClient.refreshToken(authInfo.getOpenID(), authInfo.getRefreshToken());
	}

	private static void process(int pageIndex) throws Exception {
		// log.info("Call getLoanList, pageIndex: " + pageIndex);
		Result result = null;
		try {
			result = OpenApiClient.send(GET_LOAN_LIST_URL,
					new PropertyObject("PageIndex", pageIndex, ValueTypeEnum.Int16));
		} catch (Exception e) {
			log.error("Failed to send request", e);
			return;
		}

		if (result.isSucess()) {
			JSONObject json = new JSONObject(result.getContext());
			int length = json.getInt("Result");
			if (length > 0) {
				JSONArray array = json.getJSONArray("LoanInfos");
				if (array.length() == 0) {
					throw new Exception("No loan Infos found");
				}

				for (int i = 0; i < array.length(); i++) {
					JSONObject loanObj = array.getJSONObject(i);
					long listingId = loanObj.getLong("ListingId");
					String creditCode = loanObj.getString("CreditCode");
					long amount = loanObj.getInt("Amount");
					int rate = loanObj.getInt("Rate");
					int months = loanObj.getInt("Months");
					int payWay = loanObj.getInt("PayWay");

					if (isAcceptable(listingId, creditCode, amount, rate, months, payWay)) {
						long buyAmount = amount < EACH_AMOUNT ? amount : EACH_AMOUNT;
						bidding(listingId, buyAmount);
						log.info("Called bidding, listingId: " + listingId + ", buyAmount: " + buyAmount
								+ ", creditCode: " + creditCode + ", rate: " + rate + ", months: " + months
								+ ", payWay: " + payWay);
					}
				}
			} else {
				throw new Exception("No result");
			}
		} else {
			log.error("Error Msg: " + result.getErrorMessage());
			throw new Exception(result.getErrorMessage());
		}
	}

	private static boolean isAcceptable(long listingId, String creditCode, long amount, int rate, int months,
			int payWay) {
		if (((rate > 12 && months == 12) || (rate > 13 && months == 18))
				&& ("AAA".equalsIgnoreCase(creditCode) || "AA".equalsIgnoreCase(creditCode))
				&& payWay == CONDITION_PAYWAY) {
			return true;
		}
		return false;
	}

	private static String autoLogin() throws Exception {
		String token = null;
		log.info("Call autoLogin");

		Result result = OpenApiClient.send(AUTO_LOGIN_URL, authInfo.getAccessToken(),
				new PropertyObject("Timestamp", new Date(), ValueTypeEnum.DateTime));
		if (result.isSucess()) {
			log.info("Context: " + result.getContext());
			JSONObject json = new JSONObject(result.getContext());
			token = json.getString("Token");
			log.info("Token: " + token);
		} else {
			log.error("Error Msg: " + result.getErrorMessage());
		}

		return token;
	}

	private static void bidding(long listingId, long amount) {
		try {
			Result result = OpenApiClient.send(BIDDING_URL, authInfo.getAccessToken(),
					new PropertyObject("ListingId", listingId, ValueTypeEnum.Int16),
					new PropertyObject("Amount", amount, ValueTypeEnum.Int16));
			if (result.isSucess()) {
				JSONObject json = new JSONObject(result.getContext());
				int resultCode = json.getInt("Result");
				if (resultCode != 0) {
					String msg = "Failed to bidding, listingId: " + listingId;
					switch (resultCode) {
					case -1:
						msg += " 未知异常, code: -1";
						break;
					case 1002:
						msg += " 用户信息不存在, code: 1002";
						break;
					case 1001:
						msg += " 用户编号异常, code: 1001";
						break;
					case 2001:
						msg += " 标的编号异常, code: 2001";
						break;
					case 2002:
						msg += " 标的不存在, code: 2002";
						break;
					case 3001:
						msg += " 单笔投标金额只能是50-10000的整数, code: 3001";
						break;
					case 3002:
						msg += " 累计投标金额不能＞20000元, code: 3002";
						break;
					case 3003:
						msg += " 累计投标金额不能＞标的金额的30%, code: 3003";
						break;
					case 3004:
						msg += " 不能给自己投标, code: 3004";
						break;
					case 3005:
						msg += " 已满标, code: 3005";
						break;
					case 4001:
						msg += " 账户余额不足，请先充值, code: 4001";
						break;
					}
					log.info(msg);
					return;
				}
				long aListingId = json.getLong("ListingId");
				int aAmount = json.getInt("ParticipationAmount");
				log.info("Bidding Successfully!! Actual Bidding ListingId: " + aListingId + ", Participation Amount: "
						+ aAmount);
				GOT_AMOUNT += aAmount;
				GOT_BIDDING_LISTING_IDS.add(aListingId);
			} else {
				log.error("Error Msg: " + result.getErrorMessage());
			}
		} catch (Exception e) {
			log.error("Failed", e);
		}
	}
}
