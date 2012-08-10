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

		// ===========================================
		Console.WriteLine(" === Start To Read JSON from File: metadata");
		FileInfo file = new FileInfo("metadata.txt");
		if (file == null) {
			Console.WriteLine("Failed to load game metadata resource : ");
			return;
		}
		TextReader tr = new StreamReader(file.OpenRead());
		SimpleJsonReader jReader = new SimpleJsonReader();
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
			return;
		}

		// write to another file
		SimpleJsonWriter writer = new SimpleJsonWriter();
		writer.write("metadata.txt.copy", dMeta);

		// ===========================================

		Dictionary<string, object> duser = jReader.list2arrayWrapper(jReader.Read(new StreamReader(new FileInfo("user.dat").OpenRead())));
		Console.WriteLine(duser["achievment"]);
		object[] dach = (object[])duser["achievment"];
		Console.WriteLine(dach[0]);
		Console.WriteLine(dach[1]);

		return;
	}
}

