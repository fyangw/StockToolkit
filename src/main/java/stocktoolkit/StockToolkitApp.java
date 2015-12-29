package stocktoolkit;

import java.awt.BorderLayout;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.swing.JFrame;

public class StockToolkitApp {
	private static final int OPR_LT = 0;
	private static final int OPR_EQ = 1;
	private static final int OPR_GT = 2;
	private String code;
	private String ind;
	private Integer opr;
	private Integer targetValue;

	static String[] colNames = {
			"market",
			"code",
			"name",
			"gtb5a",
			"gtb4a",
			"gtb3a",
			"gtb2a",
			"gtb1a",
			"gts1a",
			"gts2a",
			"gts3a",
			"gts4a",
			"gts5a",
			"gtb5b",
			"gtb4b",
			"gtb3b",
			"gtb2b",
			"gtb1b",
			"gts1b",
			"gts2b",
			"gts3b",
			"gts4b",
			"gts5b",
	};
	
	@SuppressWarnings("serial")
	static HashMap<String, Integer> colNameMap = new HashMap<String, Integer>() {	{
		for (int i = 0; i < colNames.length; i ++) {
			put(colNames[i], i);
		}
	}};

	public StockToolkitApp(String[] args) {
		for (String arg:args) {
			if (!arg.startsWith("--")) {
				throw new IllegalArgumentException("Unknown parameter: " + arg);
			} else {
				String[] arr = arg.substring(2).split(":");
				switch (arr[0]) {
				case "code":
					code = arr[1];
					break;
				case "ind":
					ind = arr[1];
					switch (arr[2]) {
					case "lt":
						opr = OPR_LT;
						break;
					case "eq":
						opr = OPR_EQ;
						break;
					case "gt":
						opr = OPR_GT;
						break;
					default:
						throw new IllegalArgumentException("Unknown opr: " + arg);
					}
					targetValue = Integer.parseInt(arr[3]);
					break;
				default:
					throw new IllegalArgumentException("Unknown parameter: " + arg);
				}
			}
		}
		if (code == null || ind == null || opr == null || targetValue == null) {
			printUsage();
			System.exit(-1);
		}
	}

	private void printUsage() {
		System.out.println("Options:\n"
				+ "--code:<the code of your quote, i.e. 601818>\n"
				+ "--ind:<the index name, i.e. gts5b>\n"
				+ ":<lt for less than, gt for great than, eq for equal>\n"
				+ ":<value of the condition>\n");
	}

	static public void main (String[] args) {
		new StockToolkitApp(args).run();
	}

	private void run() {
		try {
			
			initGui();
			
			
			System.out.print("Monitoring ");
			int value = 0;
			for (boolean result = false; !result; ) {
				String content = readQuote(code);
				String[] cols = parse(content);
				
				value = Integer.parseInt(cols[colNameMap.get(ind)]);
				switch (opr) {
				case OPR_LT:
					result = value < targetValue;
					break;
				case OPR_EQ:
					result = value == targetValue;
					break;
				case OPR_GT:
					result = value > targetValue;
					break;
				}
				
				if (!result) {
					System.out.print(".");
					Thread.sleep(1000 * 5);
				}
			}
			alert("\nThe condition reached, target " + targetValue + " " 
					+ (opr == OPR_LT ? "<" 
						: opr == OPR_EQ ? "=" 
						: opr == OPR_GT ? ">" 
						: "unknown")
				+ " current " + value);
			
			
			
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
	}

	
	private void initGui() {
	}

	private void alert(String str) {
		System.out.println (str);
	}

	private String[] parse(String content) {
		String[] arr = content
		.replace("\\]\\}\\)$", "")
		.split("\"Value\":\\[")[1].split(",");
		
		for (int i = 0; i < arr.length; i ++) {
			arr[i] = arr[i].replace("\"", "");
		}
		
		return arr;
	}

	private String readQuote(String code2) throws Throwable {
		URL url = new URL("http://nuff.eastmoney.com/EM_Finance2015TradeInterface/JS.ashx?id=" + code + "&token=1&cb=cb&callback=cb&_=1");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3000);  
		conn.setReadTimeout(3000);  
		InputStreamReader reader = new InputStreamReader(conn.getInputStream(), "utf-8");
		StringBuilder builder = new StringBuilder();
		int x;
		while((x = reader.read()) != -1 && (char) x != ')')
			builder.append((char) x);
		if (x != -1)
			builder.append((char) x);
		reader.close();
		return builder.toString();
	}
}
