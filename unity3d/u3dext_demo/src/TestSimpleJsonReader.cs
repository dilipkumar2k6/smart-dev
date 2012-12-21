using System;
using System.IO;
using System.Text;
using System.Collections;
using System.Collections.Generic;
using System.Runtime;
using u3dext;

public class TestSimpleJsonReader {

	public TestSimpleJsonReader () {
	}

	public static void Main (String[] args) {
		TestSimpleJsonReader ins = new TestSimpleJsonReader();

		Dictionary<string, object> dataUser = ins.testReadUserData("user.dat");
		dataUser.Add("music", true);
		ins.testWriteJsonFile(dataUser, "user.dat.copy");

		dataUser = ins.testReadUserData("user.dat.copy");
		dataUser["music"] = false;
		ins.testWriteJsonFile(dataUser, "user.dat.copy");

//		Dictionary<string, object> data = ins.testReadJsonFile();
//		ins.testWriteJsonFile(data, "metadata.txt.copy");
		return;
	}

	private Dictionary<string, object> testReadUserData(string fileName) {
		Console.WriteLine(" === Start To Read JSON from File: " + fileName +" === ");
		SimpleJsonReader jReader = new SimpleJsonReader();
		StreamReader sr = new StreamReader(new FileInfo(fileName).OpenRead());
		Dictionary<string, object> duser = 
			jReader.Read(sr);
		Console.WriteLine(duser["achievment"]);
		IList dach = (IList)duser["achievment"];
		Console.WriteLine(dach[0]);
		Console.WriteLine(dach[1]);
		sr.Close();
		return duser;
	}

	private Dictionary<string, object> testReadJsonFile() {
		Console.WriteLine(" === Start To Read JSON from File: metadata.txt === ");
		FileInfo file = new FileInfo("metadata.txt");
		if (file == null) {
			Console.WriteLine("Failed to load game metadata resource : ");
			return null;
		}
		TextReader tr = new StreamReader(file.OpenRead());
		SimpleJsonReader jReader = new SimpleJsonReader();
		jReader.log = Console.WriteLine;
		Dictionary<string, object> dMeta = jReader.Read(tr);

		Console.WriteLine("==================================");

		Dictionary<string, object> dMeta2 = jReader.list2arrayWrapper(dMeta);

		Console.WriteLine("stage: " + dMeta2["stage"].GetType());

		Dictionary<string,object>[] data = (Dictionary<string,object>[])dMeta2["stage"];
		Console.WriteLine(data);

		Console.WriteLine(data[0].GetType());
		Console.WriteLine(data[0]["id"]);

		Console.WriteLine("level: " + data[0]["level"]);
		if (dMeta.Count == 0) {
			Console.WriteLine("Failed to read metadata to JSON object");
			return null;
		}
		return dMeta;
	}

	private void testWriteJsonFile(Dictionary<string, object> dMeta, string fileName) {
		Console.WriteLine(" === Start To Write JSON to File: " + fileName + " == ");
		// write to another file
		SimpleJsonWriter writer = new SimpleJsonWriter();
		writer.log = Console.WriteLine;
		writer.write(fileName, dMeta);

	}
}

